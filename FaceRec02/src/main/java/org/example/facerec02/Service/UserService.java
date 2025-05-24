package org.example.facerec02.Service;

import org.example.facerec02.Entity.FaceRecognitionHistory;
import org.example.facerec02.Entity.User;
import org.example.facerec02.Repository.FaceRecognitionHistoryRepository;
import org.example.facerec02.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FaceRecognitionHistoryRepository historyRepository;

    // 注册用户
    @Transactional
    public User registerUser(User user) {
        logger.info("开始注册用户: {}", user.getUsername());
        
        // 验证用户名格式
        if (!isValidUsername(user.getUsername())) {
            logger.warn("用户名格式不正确: {}", user.getUsername());
            throw new RuntimeException("用户名格式不正确：不能是邮箱格式，长度3-20个字符，只能包含字母、数字和下划线");
        }
        
        // 检查用户名是否已存在
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            logger.warn("用户名已存在: {}", user.getUsername());
            throw new RuntimeException("用户名已存在");
        }
        User savedUser = userRepository.save(user);
        logger.info("用户注册成功: {}", savedUser.getUsername());
        return savedUser;
    }

    // 验证用户名格式
    private boolean isValidUsername(String username) {
        // 用户名不能是邮箱格式
        if (username.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")) {
            return false;
        }
        // 用户名长度在3-20个字符之间
        if (username.length() < 3 || username.length() > 20) {
            return false;
        }
        // 用户名只能包含字母、数字和下划线
        return username.matches("^[a-zA-Z0-9_]+$");
    }

    // 用户登录
    public Optional<User> loginUser(String usernameOrEmail, String password) {
        logger.info("尝试登录用户: {}", usernameOrEmail);
        Optional<User> userOpt;
        
        // 判断输入是否为邮箱格式
        if (usernameOrEmail.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")) {
            // 使用邮箱登录
            userOpt = userRepository.findByEmail(usernameOrEmail);
        } else {
            // 使用用户名登录
            userOpt = userRepository.findByUsername(usernameOrEmail);
        }
        
        // 验证密码
        userOpt = userOpt.filter(user -> user.getPassword().equals(password));
        
        if (userOpt.isPresent()) {
            logger.info("用户登录成功: {}", usernameOrEmail);
        } else {
            logger.warn("用户登录失败: {}", usernameOrEmail);
        }
        return userOpt;
    }

    // 根据ID获取用户
    public Optional<User> getUserById(Long userId) {
        return userRepository.findById(userId);
    }

    // 保存识别历史
    @Transactional
    public FaceRecognitionHistory saveHistory(FaceRecognitionHistory history) {
        return historyRepository.save(history);
    }

    // 获取历史记录
    public List<FaceRecognitionHistory> getHistory(Long userId) {
        return historyRepository.findByUserId(userId);
    }

    // 删除历史记录
    @Transactional
    public boolean deleteHistory(Long historyId, Long userId) {
        Optional<FaceRecognitionHistory> historyOpt = historyRepository.findById(historyId);
        if (historyOpt.isPresent()) {
            FaceRecognitionHistory history = historyOpt.get();
            // 验证记录是否属于当前用户
            if (history.getUser().getId().equals(userId)) {
                historyRepository.delete(history);
                logger.info("删除历史记录成功: {}", historyId);
                return true;
            }
        }
        logger.warn("删除历史记录失败: {}", historyId);
        return false;
    }

    @Transactional
    public boolean changePassword(Long userId, String oldPassword, String newPassword) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent() && userOpt.get().getPassword().equals(oldPassword)) {
            User user = userOpt.get();
            user.setPassword(newPassword);
            userRepository.save(user);
            logger.info("用户密码修改成功: {}", user.getUsername());
            return true;
        }
        logger.warn("用户密码修改失败: {}", userId);
        return false;
    }
}

