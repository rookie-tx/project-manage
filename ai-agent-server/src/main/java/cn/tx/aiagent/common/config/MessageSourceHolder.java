package cn.tx.aiagent.common.config;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

@Component
public class MessageSourceHolder {

    private static MessageSource messageSource;

    public MessageSourceHolder(MessageSource messageSource) {
        MessageSourceHolder.messageSource = messageSource;
    }

    public static String getMessage(String key, Object... args) {
        if (messageSource == null) {
            return key;
        }
        return messageSource.getMessage(key, args, key, LocaleContextHolder.getLocale());
    }
}
