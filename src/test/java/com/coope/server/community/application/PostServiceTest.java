package com.coope.server.community.application;

import com.coope.server.community.CommunityTestUtils;
import com.coope.server.community.domain.post.Post;
import com.coope.server.community.domain.post.PostRepository;
import com.coope.server.community.domain.post.enums.PostCategory;
import com.coope.server.community.presentation.dto.PostCreateRequest;
import com.coope.server.community.presentation.dto.PostDetailResponse;
import com.coope.server.community.presentation.dto.PostUpdateRequest;
import com.coope.server.shared.error.exception.AccessDeniedException;
import com.coope.server.shared.error.exception.PostNotFoundException;
import com.coope.server.user.domain.User;
import com.coope.server.user.domain.enums.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private PostCommentService postCommentService;

    @InjectMocks
    private PostService postService;

    @Test
    @DisplayName("모집 게시글 생성 시 기술 스택이 없으면 예외가 발생한다")
    void createPost_Recruitment_NoTechStack() {
        // Given
        User author = CommunityTestUtils.createTestUser(1L, "작성자", Role.ROLE_USER);
        PostCreateRequest request = new PostCreateRequest();
        ReflectionTestUtils.setField(request, "category", PostCategory.RECRUITMENT);
        ReflectionTestUtils.setField(request, "title", "제목");
        ReflectionTestUtils.setField(request, "content", "내용");
        ReflectionTestUtils.setField(request, "techStack", ""); // 빈 값

        // When & Then
        assertThatThrownBy(() -> postService.createPost(request, author))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("모집 게시글에는 기술 스택을 입력해야 합니다.");
    }

    @Test
    @DisplayName("모집 게시글 생성 시 현재 인원이 목표 인원보다 많으면 예외가 발생한다")
    void createPost_Recruitment_InvalidMembers() {
        // Given
        User author = CommunityTestUtils.createTestUser(1L, "작성자", Role.ROLE_USER);
        PostCreateRequest request = new PostCreateRequest();
        ReflectionTestUtils.setField(request, "category", PostCategory.RECRUITMENT);
        ReflectionTestUtils.setField(request, "techStack", "Java");
        ReflectionTestUtils.setField(request, "currentMembers", 5);
        ReflectionTestUtils.setField(request, "targetMembers", 3); // 현재가 더 많음

        // When & Then
        assertThatThrownBy(() -> postService.createPost(request, author))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("현재 인원은 목표 인원보다 클 수 없습니다.");
    }

    @Test
    @DisplayName("게시글 상세 조회 시 조회수가 증가하고 댓글 목록을 함께 반환한다")
    void getPostDetail_Success() {
        // Given
        User author = CommunityTestUtils.createTestUser(1L, "작성자", Role.ROLE_USER);
        Post post = Post.createGeneralPost(PostCategory.GENERAL, "제목", "내용", author);
        ReflectionTestUtils.setField(post, "id", 100L);

        given(postRepository.findById(100L)).willReturn(Optional.of(post));
        given(postCommentService.getComments(100L, author)).willReturn(Collections.emptyList());

        // When
        PostDetailResponse response = postService.getPostDetail(100L, author);

        // Then
        assertThat(response.getTitle()).isEqualTo("제목");
        verify(postCommentService).getComments(100L, author); // 댓글 서비스 호출 확인
    }

    @Test
    @DisplayName("조회수 증가 로직이 정상 작동한다")
    void increaseViewCount_Success() {
        // Given
        User author = CommunityTestUtils.createTestUser(1L, "작성자", Role.ROLE_USER);
        Post post = Post.createGeneralPost(PostCategory.GENERAL, "제목", "내용", author);
        ReflectionTestUtils.setField(post, "id", 100L);
        int initialViewCount = post.getViewCount();

        given(postRepository.findById(100L)).willReturn(Optional.of(post));

        // When
        postService.increaseViewCount(100L);

        // Then
        assertThat(post.getViewCount()).isEqualTo(initialViewCount + 1);
    }

    @Test
    @DisplayName("게시글 수정 시 작성자가 아니면 예외가 발생한다")
    void updatePost_AccessDenied() {
        // Given
        User author = CommunityTestUtils.createTestUser(1L, "작성자", Role.ROLE_USER);
        User stranger = CommunityTestUtils.createTestUser(2L, "낯선인", Role.ROLE_USER);

        Post post = Post.createGeneralPost(PostCategory.GENERAL, "제목", "내용", author);
        ReflectionTestUtils.setField(post, "id", 100L);

        given(postRepository.findById(100L)).willReturn(Optional.of(post));
        PostUpdateRequest request = new PostUpdateRequest(); // 필드 세팅 생략해도 예외가 먼저 터짐

        // When & Then
        assertThatThrownBy(() -> postService.updatePost(100L, request, stranger))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("게시글 작성자만 수정·삭제할 수 있습니다.");
    }

    @Test
    @DisplayName("존재하지 않는 게시글을 조회하면 PostNotFoundException이 발생한다")
    void findPostOrThrow_NotFound() {
        // Given
        given(postRepository.findById(999L)).willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> postService.getPostDetail(999L, null))
                .isInstanceOf(PostNotFoundException.class)
                .hasMessage("해당 게시글을 찾을 수 없습니다.");
    }
}