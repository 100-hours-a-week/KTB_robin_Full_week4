package ktb3.fullstack.week4.service.user;

import ktb3.fullstack.week4.Security.service.AppPasswordEncoder;
import ktb3.fullstack.week4.domain.images.ProfileImage;
import ktb3.fullstack.week4.domain.users.User;
import ktb3.fullstack.week4.dto.users.JoinRequest;
import ktb3.fullstack.week4.repository.images.ProfileImageRepository;
import ktb3.fullstack.week4.repository.users.UserRepository;
import ktb3.fullstack.week4.service.availabilities.AvailabilityService;
import ktb3.fullstack.week4.service.errors.ErrorCheckServiceImpl;
import ktb3.fullstack.week4.service.images.ImageDomainBuilder;
import ktb3.fullstack.week4.service.images.ProfileImageService;
import ktb3.fullstack.week4.service.users.UserDeleteFacade;
import ktb3.fullstack.week4.service.users.UserDomainBuilder;
import ktb3.fullstack.week4.service.users.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @InjectMocks
    UserService userService;

    @Mock
    UserDomainBuilder userDomainBuilder;
    @Mock
    ImageDomainBuilder imageDomainBuilder;
    @Mock
    AppPasswordEncoder passwordEncoder;
    @Mock
    ErrorCheckServiceImpl errorCheckService;
    @Mock
    AvailabilityService availabilityService;
    @Mock
    ProfileImageService profileImageService;
    @Mock
    UserRepository userRepository;
    @Mock
    ProfileImageRepository profileImageRepository;
    @Mock
    UserDeleteFacade userDeleteFacade;


    @Test
    @DisplayName("회원가입의 비즈니스로직 검증")
    void register_logic_test() {
        //given
        JoinRequest dto = JoinRequest.builder()
                .email("test@email.com")
                .password("password123")
                .nickname("nickname")
                .build();

        MockMultipartFile image = new MockMultipartFile(
                "profile_image",
                "original.jpg",
                "image/jpeg",
                "dummy content".getBytes()
        );
        String hashedPassword = "hashedPassword";
        given(passwordEncoder.encode(dto.getPassword())).willReturn(hashedPassword);

        User user = User.builder().build();
        given(userDomainBuilder.buildUser(hashedPassword, dto)).willReturn(user);

        String profileImageUrl = "static/assets/profile/12312ab123ab1original.jpg";
        given(profileImageService.makeImagePathString(image)).willReturn(profileImageUrl);

        String expected = "/assets/profile/12312ab123ab1original.jpg";

        ProfileImage profileImage = ProfileImage.builder().build();
        given(imageDomainBuilder.buildProfileImage(user, expected)).willReturn(profileImage);

        //when
        userService.register(dto,image);

        //then
        verify(availabilityService).checkRegisterAvailability(dto, image);
        verify(profileImageService).transferImageToLocalDirectory(image, profileImageUrl);
        verify(imageDomainBuilder).buildProfileImage(user, expected);
        verify(profileImageRepository).save(profileImage);
        verify(userRepository).save(user);
    }
}
