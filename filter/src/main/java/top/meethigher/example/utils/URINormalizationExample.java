package top.meethigher.example.utils;

import java.net.URI;
import java.net.URISyntaxException;
import javax.servlet.http.HttpServletRequest;

public class URINormalizationExample {

    /**
     * uri标准化
     *
     * @param originalURI 原始uri
     * @return {@link String}
     * @throws URISyntaxException uri语法异常
     */
    public static String normalizeURI(String originalURI) throws URISyntaxException {
        URI uri = new URI(originalURI);
        URI normalizedURI = uri.normalize();
        return normalizedURI.toString();
    }

    public static void main(String[] args) throws URISyntaxException {
        String originalURI = "/a/b/../../test/test1";
        String normalizedURI = normalizeURI(originalURI);
        System.out.println("Original URI: " + originalURI);
        System.out.println("Normalized URI: " + normalizedURI);
    }
}