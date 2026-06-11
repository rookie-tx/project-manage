package cn.tx.aiagent.user.domain;

import cn.tx.aiagent.common.api.ErrorCode;
import cn.tx.aiagent.common.exception.BusinessException;
import cn.tx.aiagent.login.domain.PasswordHasher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserDomainServiceTest {

    private UserRepository userRepository;
    private PasswordHasher passwordHasher;
    private UserDomainService service;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        passwordHasher = mock(PasswordHasher.class);
        service = new UserDomainService(userRepository, passwordHasher);
    }

    @Test
    void shouldCreateUserSuccessfully() {
        when(userRepository.findByUsername("zhangsan")).thenReturn(Optional.empty());
        when(passwordHasher.hash("123456")).thenReturn("$2a$hashed");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User user = service.createUser("zhangsan", "123456", "test@example.com", "13800138000");

        assertNotNull(user);
        assertEquals("zhangsan", user.getUsername());
        assertEquals("$2a$hashed", user.getPasswordHash());
        assertEquals("test@example.com", user.getEmail());
        assertEquals("13800138000", user.getPhone());
        assertEquals(UserStatus.ACTIVE, user.getStatus());
        assertFalse(user.isDeleted());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void shouldThrowExceptionWhenUsernameDuplicate() {
        User existing = User.fromPersistence(1L, "zhangsan", "$2a$old", null, null, 1, 0, LocalDateTime.now());
        when(userRepository.findByUsername("zhangsan")).thenReturn(Optional.of(existing));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.createUser("zhangsan", "123456", null, null));

        assertEquals(ErrorCode.USERNAME_DUPLICATE.getCode(), ex.getErrorCode().getCode());
    }

    @Test
    void shouldSoftDeleteUserSuccessfully() {
        User user = User.fromPersistence(5L, "lisi", "$2a$hash", null, null, 1, 0, LocalDateTime.now());
        when(userRepository.findById(5L)).thenReturn(Optional.of(user));

        service.softDeleteUser(5L);

        verify(userRepository).softDelete(5L);
    }

    @Test
    void shouldThrowExceptionWhenSoftDeleteNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.softDeleteUser(999L));

        assertEquals(ErrorCode.USER_NOT_FOUND.getCode(), ex.getErrorCode().getCode());
    }

    @Test
    void shouldThrowExceptionWhenSoftDeleteAlreadyDeleted() {
        User user = User.fromPersistence(5L, "lisi", "$2a$hash", null, null, 1, 1, LocalDateTime.now());
        when(userRepository.findById(5L)).thenReturn(Optional.of(user));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.softDeleteUser(5L));

        assertEquals(ErrorCode.USER_DELETED.getCode(), ex.getErrorCode().getCode());
    }

    @Test
    void shouldUpdateUserStatus() {
        User user = User.fromPersistence(3L, "wangwu", "$2a$hash", null, null, 1, 0, LocalDateTime.now());
        when(userRepository.findById(3L)).thenReturn(Optional.of(user));

        service.updateUser(3L, null, null, 0, null);

        assertEquals(UserStatus.DISABLED, user.getStatus());
        verify(userRepository).update(user);
    }

    @Test
    void shouldUpdateUserWithRoles() {
        User user = User.fromPersistence(3L, "wangwu", "$2a$hash", null, null, 1, 0, LocalDateTime.now());
        when(userRepository.findById(3L)).thenReturn(Optional.of(user));

        service.updateUser(3L, null, null, null, List.of(1L, 2L));

        verify(userRepository).update(user);
        verify(userRepository).replaceUserRoles(3L, List.of(1L, 2L));
    }
}
