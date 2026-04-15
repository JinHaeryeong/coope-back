package com.coope.server.community.application;

import com.coope.server.community.domain.comment.PostComment;
import com.coope.server.community.domain.comment.PostCommentRepository;
import com.coope.server.community.domain.post.Post;
import com.coope.server.community.domain.post.PostRepository;
import com.coope.server.community.presentation.dto.CommentCreateRequest;
import com.coope.server.community.presentation.dto.CommentResponse;
import com.coope.server.community.presentation.dto.CommentUpdateRequest;
import com.coope.server.shared.error.exception.AccessDeniedException;
import com.coope.server.user.domain.User;
import com.coope.server.user.domain.enums.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static com.coope.server.community.CommunityTestUtils.createTestUser;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class PostCommentServiceTest {

    @Mock
    private PostCommentRepository postCommentRepository;

    @Mock
    private PostRepository postRepository;

    @InjectMocks
    private PostCommentService postCommentService;

    @Test
    @DisplayName("댓글 작성자가 아닌 사람이 수정하면 예외가 발생한다")
    void updateComment_AccessDenied() {
        // Given
        User author = createTestUser(1L, "작성자", Role.ROLE_USER);
        User stranger = createTestUser(2L, "낯선인", Role.ROLE_USER);

        Post post = Post.builder().author(author).build();
        ReflectionTestUtils.setField(post, "id", 100L); // postId 설정

        PostComment comment = PostComment.create("원래 내용", false, post, author);
        ReflectionTestUtils.setField(comment, "id", 1L); // commentId 설정

        // 가짜 레포지토리 동작 정의
        given(postCommentRepository.findById(1L)).willReturn(Optional.of(comment));

        // When & Then
        // 100번 게시글의 1번 댓글을 'stranger'가 수정하려고 할 때
        CommentUpdateRequest request = new CommentUpdateRequest();
        ReflectionTestUtils.setField(request, "content", "수정내용"); // DTO가 Getter만 있을 때

        assertThatThrownBy(() ->
                postCommentService.updateComment(100L, 1L, request, stranger)
        ).isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @DisplayName("댓글 작성자가 수정하면 성공한다")
    void updateComment_Success() {
        // Given
        User author = createTestUser(1L, "작성자", Role.ROLE_USER);
        Post post = Post.builder().author(author).build();
        ReflectionTestUtils.setField(post, "id", 100L);

        PostComment comment = PostComment.create("원래 내용", false, post, author);
        ReflectionTestUtils.setField(comment, "id", 1L);

        given(postCommentRepository.findById(1L)).willReturn(Optional.of(comment));

        CommentUpdateRequest request = new CommentUpdateRequest();
        ReflectionTestUtils.setField(request, "content", "수정된 내용");

        // When
        CommentResponse response = postCommentService.updateComment(100L, 1L, request, author);

        assertThat(response.getContent()).isEqualTo("수정된 내용");
        assertThat(comment.getContent()).isEqualTo("수정된 내용"); // 실제 엔티티도 바뀌었는지 확인
    }

    @Test
    @DisplayName("댓글 작성자가 아니더라도 게시글 주인이면 댓글을 삭제할 수 있다")
    void deleteComment_ByPostOwner() {
        // Given
        User postOwner = createTestUser(1L, "게시글주인", Role.ROLE_USER);
        User commentAuthor = createTestUser(2L, "댓글작성자", Role.ROLE_USER);

        Post post = Post.builder().author(postOwner).build();
        ReflectionTestUtils.setField(post, "id", 100L);

        PostComment comment = PostComment.create("댓글내용", false, post, commentAuthor);
        ReflectionTestUtils.setField(comment, "id", 1L);

        given(postCommentRepository.findById(1L)).willReturn(Optional.of(comment));

        // When & Then (에러가 터지지 않아야 성공)
        postCommentService.deleteComment(100L, 1L, postOwner);

        // postRepository.decrementCommentCount와 postCommentRepository.delete가 호출되었는지 검증
        verify(postRepository).decrementCommentCount(100L);
        verify(postCommentRepository).delete(comment);
    }

    @Test
    @DisplayName("댓글 목록 조회 시 권한이 없는 비밀 댓글은 마스킹되어야 한다")
    void getComments_MaskingTest() {
        // Given
        User author = createTestUser(1L, "작성자", Role.ROLE_USER);
        User stranger = createTestUser(2L, "낯선인", Role.ROLE_USER);
        Post post = Post.builder().author(author).build();
        ReflectionTestUtils.setField(post, "id", 100L);

        PostComment secretComment = PostComment.create("비밀내용", true, post, author);

        // postCommentRepository가 댓글 리스트를 반환하도록 설정
        given(postCommentRepository.findByPostIdWithAuthor(100L)).willReturn(List.of(secretComment));

        // When
        List<CommentResponse> responses = postCommentService.getComments(100L, stranger);

        // Then
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getContent()).isEqualTo("비밀 댓글입니다."); // 마스킹 확인
        assertThat(responses.get(0).isMasked()).isTrue();
    }

    @Test
    @DisplayName("댓글을 생성하면 저장되고 게시글의 댓글 카운트가 증가한다")
    void createComment_Success() {
        // Given
        User author = createTestUser(1L, "작성자", Role.ROLE_USER);
        Post post = Post.builder().author(author).build();
        ReflectionTestUtils.setField(post, "id", 100L);

        CommentCreateRequest request = new CommentCreateRequest();
        ReflectionTestUtils.setField(request, "content", "새 댓글");
        ReflectionTestUtils.setField(request, "isPrivate", false);

        given(postRepository.findById(100L)).willReturn(Optional.of(post));
        // save 메서드 호출 시 전달받은 객체를 그대로 반환하도록 설정
        given(postCommentRepository.save(any(PostComment.class))).willAnswer(invocation -> invocation.getArgument(0));

        // When
        postCommentService.createComment(100L, request, author);

        // Then
        verify(postRepository).incrementCommentCount(100L); // 카운트 증가 함수 호출 확인
        verify(postCommentRepository).save(any(PostComment.class)); // 저장 함수 호출 확인
    }
}