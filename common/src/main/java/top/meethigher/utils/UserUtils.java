package top.meethigher.utils;

import lombok.extern.slf4j.Slf4j;

/**
 * 用户工具类
 *
 * @author chenchuancheng
 * @date 2023/08/03 15:56
 */
@Slf4j
public class UserUtils {

    private static final ThreadLocal<String> threadLocal = new ThreadLocal<>();

    public static void add(String info) {
        log.info("add");
        threadLocal.set(info);
    }

    public static void remove() {
        log.info("remove");
        threadLocal.remove();
    }

    public static String get() {
        log.info("get");
        return threadLocal.get();
    }
}
