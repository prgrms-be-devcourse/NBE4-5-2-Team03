package com.example.Flicktionary.domain.post.service;

import com.example.Flicktionary.domain.post.dto.PostCreateRequestDto;
import com.example.Flicktionary.domain.post.dto.PostResponseDto;
import com.example.Flicktionary.domain.post.dto.PostUpdateRequestDto;
import com.example.Flicktionary.domain.post.entity.Post;
import com.example.Flicktionary.domain.post.repository.PostRepository;
import com.example.Flicktionary.domain.user.entity.UserAccount;
import com.example.Flicktionary.domain.user.entity.UserAccountType;
import com.example.Flicktionary.domain.user.repository.UserAccountRepository;
import com.example.Flicktionary.global.dto.PageDto;
import com.example.Flicktionary.global.exception.ServiceException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("게시글 서비스 테스트")
@ExtendWith(MockitoExtension.class)
public class PostServiceTest {

    @InjectMocks
    private PostService postService;

    @Mock
    private PostRepository postRepository;

    @Mock
    private UserAccountRepository userAccountRepository;

    /* 테스트용 변수 설정 */
    // 테스트용 유저
    private final UserAccount testUser = new UserAccount(
            1L,
            "테스트용 유저",
            "test12345",
            "test@email.com",
            "테스트 유저",
            UserAccountType.USER);

    // 테스트용 요청 게시글 Dto
    private final PostCreateRequestDto requestDto = PostCreateRequestDto.builder()
            .userAccountId(testUser.getId())
            .title("테스트 제목")
            .content("테스트 내용")
            .isSpoiler(false)
            .build();

    // 테스트용 저장된 게시글 엔티티
    private final Post savedPost = Post.builder()
            .id(1L)
            .userAccount(testUser)
            .title("테스트 제목")
            .content("테스트 내용")
            .isSpoiler(false)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

    // 검증용 게시글 응답 Dto
    private final PostResponseDto expectedPost = PostResponseDto.builder()
            .id(1L)
            .userAccountId(testUser.getId())
            .nickname("테스트 유저")
            .title("테스트 제목")
            .content("테스트 내용")
            .isSpoiler(false)
            .build();

    @Test
    @DisplayName("게시글 생성")
    void createPost() {

        // 유저 Id로 유저를 찾은 뒤 반환 후 저장된 게시글 반환, 테스트 게시글을 생성
        when(userAccountRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(postRepository.save(any(Post.class))).thenReturn(savedPost);
        PostResponseDto testPostDto = postService.create(requestDto);

        /// 검증
        assertEquals(testPostDto.getId(), expectedPost.getId());
        assertEquals(testPostDto.getUserAccountId(), expectedPost.getUserAccountId());
        assertEquals(testPostDto.getNickname(), expectedPost.getNickname());
        assertEquals(testPostDto.getTitle(), expectedPost.getTitle());
        assertEquals(testPostDto.getContent(), expectedPost.getContent());
        assertEquals(testPostDto.getIsSpoiler(), expectedPost.getIsSpoiler());

        // PostRepository의 save 메서드가 한 번 호출되었는지 검증
        verify(postRepository).save(any(Post.class));
    }

    @Test
    @DisplayName("게시글 Id로 찾기 - 성공")
    void findPostByIdSuccess() {

        // 저장된 게시글의 id로 특정 게시글을 찾아 반환되도록 설정
        Long postId = savedPost.getId();
        when(postRepository.findById(postId)).thenReturn(Optional.of(savedPost));

        // 게시글 Id로 조회
        PostResponseDto testPostDto = postService.findById(postId);

        /// 검증
        assertEquals(testPostDto.getId(), expectedPost.getId());
        assertEquals(testPostDto.getUserAccountId(), expectedPost.getUserAccountId());
        assertEquals(testPostDto.getNickname(), expectedPost.getNickname());
        assertEquals(testPostDto.getTitle(), expectedPost.getTitle());
        assertEquals(testPostDto.getContent(), expectedPost.getContent());
        assertEquals(testPostDto.getIsSpoiler(), expectedPost.getIsSpoiler());

        // PostRepository의 findById 메서드가 호출되었는지 검증
        verify(postRepository).findById(postId);
    }

    @Test
    @DisplayName("게시글 Id로 찾기 - 실패")
    void findPostByIdFailure() {

        // 존재하지 않는 게시글 Id로 findById 호출 시 빈 Optional 반환 설정
        Long postId = 1000L;
        when(postRepository.findById(postId)).thenReturn(Optional.empty());

        /// 검증
        // findPostById 메서드를 호출했을 때 EntityNotFoundException이 발생하는지 확인
        assertThrows(ServiceException.class, () -> postService.findById(postId));

        // PostRepository의 findById 메서드가 호출되었는지 검증
        verify(postRepository).findById(postId);
    }

    @Test
    @DisplayName("게시글 목록 조회 및 검색")
    void getPostListAndSearch() {

        // 페이지 설정
        int page = 1;
        int pageSize = 10;
        Pageable pageable = PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));

        // 테스트용 게시글 목록 생성
        List<Post> posts = List.of(
                Post.builder()
                        .id(1L)
                        .userAccount(testUser)
                        .title("테스트용 게시글1")
                        .content("테스트용 게시글 내용1")
                        .isSpoiler(true)
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build(),
                Post.builder()
                        .id(2L)
                        .userAccount(testUser)
                        .title("테스트용 게시글2")
                        .content("테스트용 게시글 내용2")
                        .isSpoiler(false)
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build(),
                Post.builder()
                        .id(3L)
                        .userAccount(testUser)
                        .title("테스트용 게시글3")
                        .content("테스트용 게시글 내용3")
                        .isSpoiler(true)
                        .createdAt(LocalDateTime.now().minusDays(1))
                        .updatedAt(LocalDateTime.now().minusDays(1))
                        .build(),
                Post.builder()
                        .id(4L)
                        .userAccount(testUser)
                        .title("테스트용 게시글4")
                        .content("테스트용 게시글 내용4")
                        .isSpoiler(false)
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build(),
                Post.builder()
                        .id(5L)
                        .userAccount(testUser)
                        .title("테스트용 게시글5")
                        .content("테스트용 게시글 내용5")
                        .isSpoiler(true)
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build()
        );

        Page<Post> postPage = new PageImpl<>(posts, pageable, posts.size());

        // 전체 게시글 조회
        when(postRepository.findAll(pageable)).thenReturn(postPage);
        PageDto<PostResponseDto> testPages = postService.getPostList(page, pageSize, null, null);
        /// 검증
        // 로그 확인
        System.out.println(" ========= 게시글 목록 전체 조회 ========= ");
        for (PostResponseDto post : testPages.getItems()) {
            System.out.println("작성자 닉네임: " + post.getNickname());
            System.out.println("제목: " + post.getTitle());
            System.out.println("내용: " + post.getContent());
            System.out.println("생성 시간: " + post.getCreatedAt());
            System.out.println("스포일러(true면 스포일러, false면 스포일러 아님): " + post.getIsSpoiler());
            System.out.println(" =================================== ");
        }
        assertEquals(testPages.getItems().size(), posts.size());
        verify(postRepository).findAll(pageable);

        // 제목으로 검색
        String searchKeywordTitle = "게시글1";
        List<Post> titleSearch = posts.stream()
                .filter(post -> post.getTitle().contains(searchKeywordTitle))
                .toList();
        Page<Post> titleSearchPage = new PageImpl<>(titleSearch, pageable, titleSearch.size());
        when(postRepository.findByTitleContaining(eq(searchKeywordTitle), eq(pageable))).thenReturn(titleSearchPage);
        PageDto<PostResponseDto> titleSearchResult = postService.getPostList(page, pageSize, searchKeywordTitle, "title");
        /// 검증
        // 로그 확인
        System.out.println(" ========= 게시글 목록 조회(제목) ========= ");
        for (PostResponseDto post : titleSearchResult.getItems()) {
            System.out.println("작성자 닉네임: " + post.getNickname());
            System.out.println("제목: " + post.getTitle());
            System.out.println("내용: " + post.getContent());
            System.out.println("생성 시간: " + post.getCreatedAt());
            System.out.println("스포일러(true면 스포일러, false면 스포일러 아님): " + post.getIsSpoiler());
            System.out.println(" =================================== ");
        }
        assertEquals(titleSearchResult.getItems().size(), titleSearch.size());
        verify(postRepository).findByTitleContaining(eq(searchKeywordTitle), eq(pageable));

        // 내용으로 검색
        String searchKeywordContent = "내용2";
        List<Post> contentSearch = posts.stream()
                .filter(post -> post.getContent().contains(searchKeywordContent))
                .toList();
        Page<Post> contentSearchPage = new PageImpl<>(contentSearch, pageable, contentSearch.size());
        when(postRepository.findByContentContaining(eq(searchKeywordContent), eq(pageable))).thenReturn(contentSearchPage);
        PageDto<PostResponseDto> contentSearchResult = postService.getPostList(page, pageSize, searchKeywordContent, "content");
        /// 검증
        // 로그 확인
        System.out.println(" ========= 게시글 목록 조회(내용) ========= ");
        for (PostResponseDto post : contentSearchResult.getItems()) {
            System.out.println("작성자 닉네임: " + post.getNickname());
            System.out.println("제목: " + post.getTitle());
            System.out.println("내용: " + post.getContent());
            System.out.println("생성 시간: " + post.getCreatedAt());
            System.out.println("스포일러(true면 스포일러, false면 스포일러 아님): " + post.getIsSpoiler());
            System.out.println(" =================================== ");
        }
        assertEquals(contentSearchResult.getItems().size(), contentSearch.size());
        verify(postRepository).findByContentContaining(eq(searchKeywordContent), eq(pageable));

        // 유저 닉네임으로 검색
        String searchKeywordNickname = "유저";
        List<Post> nicknameSearch = posts.stream()
                .filter(post -> post.getUserAccount().getNickname().contains(searchKeywordNickname))
                .toList();
        Page<Post> nicknameSearchPage = new PageImpl<>(nicknameSearch, pageable, nicknameSearch.size());
        when(postRepository.findByUserAccount_NicknameContaining(eq(searchKeywordNickname), eq(pageable))).thenReturn(nicknameSearchPage);
        PageDto<PostResponseDto> nicknameSearchResult = postService.getPostList(page, pageSize, searchKeywordNickname, "nickname");
        /// 검증
        // 로그 확인
        System.out.println(" ========= 게시글 목록 조회(유저 닉네임) ========= ");
        for (PostResponseDto post : nicknameSearchResult.getItems()) {
            System.out.println("작성자 닉네임: " + post.getNickname());
            System.out.println("제목: " + post.getTitle());
            System.out.println("내용: " + post.getContent());
            System.out.println("생성 시간: " + post.getCreatedAt());
            System.out.println("스포일러(true면 스포일러, false면 스포일러 아님): " + post.getIsSpoiler());
            System.out.println(" =================================== ");
        }
        assertEquals(nicknameSearchResult.getItems().size(), nicknameSearch.size());
        verify(postRepository).findByUserAccount_NicknameContaining(eq(searchKeywordNickname), eq(pageable));
    }

    @Test
    @DisplayName("게시글 수정")
    void updatePost() {

        // 테스트용 수정할 내용 Dto 생성
        PostUpdateRequestDto updateRequestDto = PostUpdateRequestDto.builder()
                .title("수정된 제목")
                .content("수정된 내용")
                .isSpoiler(false)
                .build();

        // 확인용 엔티티 생성
        Post checkPost = Post.builder()
                .id(savedPost.getId())
                .userAccount(testUser)
                .title("수정된 제목")
                .content("수정된 내용")
                .isSpoiler(false)
                .createdAt(savedPost.getCreatedAt())
                // LocalDateTime.now()의 정밀도로 인해 50초 더하기
                .updatedAt(LocalDateTime.now().plusSeconds(50))
                .build();

        // 저장된 게시글의 id로 특정 게시글을 찾아 기존 게시글을 반환되도록 설정
        when(postRepository.findById(savedPost.getId())).thenReturn(Optional.of(savedPost));
        when(postRepository.save(any(Post.class))).thenReturn(checkPost);

        // 수정 메서드 실행 후 변수에 담기
        PostResponseDto testPage = postService.update(savedPost.getId(), updateRequestDto);

        /// 검증
        // 로그
        System.out.println(" ========= 게시글 수정 결과 ========= ");
        System.out.println("작성자 닉네임: " + testPage.getNickname());
        System.out.println("제목: " + testPage.getTitle());
        System.out.println("내용: " + testPage.getContent());
        System.out.println("생성 시간: " + testPage.getCreatedAt());
        System.out.println("수정 시간: " + testPage.getUpdatedAt());
        System.out.println("스포일러(true면 스포일러, false면 스포일러 아님): " + testPage.getIsSpoiler());
        System.out.println(" =================================== ");

        assertEquals(testPage.getTitle(), checkPost.getTitle());
        assertEquals(testPage.getContent(), checkPost.getContent());
        assertEquals(testPage.getIsSpoiler(), checkPost.getIsSpoiler());
        assertTrue(testPage.getUpdatedAt().isAfter(savedPost.getUpdatedAt()));

        // PostRepository의 findById 메서드가 호출되었는지 검증
        verify(postRepository).findById(savedPost.getId());

        // PostRepository의 save 메서드가 한 번 호출되었는지 검증
        verify(postRepository).save(any(Post.class));
    }

    @Test
    @DisplayName("게시글 삭제")
    void deletePost() {

        // 테스트용 게시글 생성
        Long postId = 1000L;
        Post existingPost = Post.builder().id(postId).build();

        // 저장된 게시글의 id로 특정 게시글을 찾아 기존 게시글을 반환되도록 설정
        when(postRepository.findById(postId)).thenReturn(Optional.of(existingPost));

        // 삭제 메서드 실행
        postService.delete(postId);

        /// 검증
        verify(postRepository).delete(existingPost);
    }
}
