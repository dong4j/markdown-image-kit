package info.dong4j.idea.plugin.weibo;

import com.google.gson.Gson;

import info.dong4j.idea.plugin.util.RSAEncodeUtils;
import info.dong4j.idea.plugin.weibo.entity.ImageInfo;
import info.dong4j.idea.plugin.weibo.entity.PreLogin;
import info.dong4j.idea.plugin.weibo.entity.UploadResp;
import info.dong4j.idea.plugin.weibo.entity.upload.Pic_1;
import info.dong4j.idea.plugin.weibo.exception.LoginFailedException;
import info.dong4j.idea.plugin.weibo.http.WbpHttpRequest;
import info.dong4j.idea.plugin.weibo.http.WbpHttpResponse;

import java.io.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import lombok.extern.slf4j.Slf4j;

import static java.net.HttpURLConnection.HTTP_OK;

/**
 * <p>Description: </p>
 *
 * @author echisan
 * @update dong4j
 * @date 2018-06-14 22:31
 */
@Slf4j
public class WbpUploadRequest implements UploadRequest {
    private WbpHttpRequest wbpHttpRequest;
    private volatile String preLoginResult;
    private static String USERNAME;
    private static String PASSWORD;
    private static final Set<String> IMAGE_EXTENSION = new HashSet<>();
    /** 重连次数 */
    private static AtomicInteger tryLoginCount = new AtomicInteger(0);
    /** 重连等待时间，每次重连失败后，重连等待时间为 = 重连次数 * 重连等待时间 */
    private static final long DEFAULT_TRY_LOGIN_TIME = 2 * 60 * 1000;
    static volatile long tryLoginTime = DEFAULT_TRY_LOGIN_TIME;
    /** 结束时间 */
    private static volatile long endTime = 0;

    WbpUploadRequest(WbpHttpRequest wbpHttpRequest, String username, String password) {
        this.wbpHttpRequest = wbpHttpRequest;
        initImageExtensionSet();
        USERNAME = username;
        PASSWORD = password;
    }

    private void initImageExtensionSet() {
        IMAGE_EXTENSION.add("jpg");
        IMAGE_EXTENSION.add("gif");
        IMAGE_EXTENSION.add("png");
    }


    private WbpHttpResponse uploadB64(String base64) throws IOException {
        String uploadUrl = "http://picupload.service.weibo.com/interface/pic_upload.php?" +
                           "ori=1&mime=image%2Fjpeg&data=base64&url=0&markpos=1&logo=&nick=0&marks=1&app=miniblog";
        return wbpHttpRequest.doPostMultiPart(uploadUrl, getUploadHeader(), base64);
    }

    private String parseBodyJson(String body) {
        int i = body.indexOf("</script>");
        return body.substring(i + 9);
    }

    @Override
    public UploadResponse upload(File image) throws IOException {

        // 判断是否已经登陆
        checkLogin();

        String fileExtension = getFileExtension(image.getName());
        String base64image = imageToBase64(image);
        WbpUploadResponse uploadResponse = new WbpUploadResponse();

        WbpHttpResponse httpResponse = uploadB64(base64image);

        // 如果返回的不是200,则直接上传就失败了
        if (httpResponse.getStatusCode() != HTTP_OK) {
            uploadResponse.setResult(UploadResponse.ResultStatus.FAILED);
            uploadResponse.setMessage(httpResponse.getBody());
            return uploadResponse;
        }

        // 检查返回的json数据
        String s = parseBodyJson(httpResponse.getBody());
        UploadResp uploadResp = new Gson().fromJson(s, UploadResp.class);

        int retCode = uploadResp.getData().getPics().getPic_1().getRet();
        if (retCode == -1) {
            // 可能是cookie过期,如果不在冷却时间内的话，再登陆一次，再上传
            if (tryReLogin()) {
                return upload(image);
            } else {
                uploadResponse.setResult(UploadResponse.ResultStatus.FAILED);
                uploadResponse.setMessage("上传失败，大概是cookie过期了，尝试重新登陆失败，目前是第" + tryLoginCount.get() + "登陆，" +
                                          "距离下次登陆时间为:" + new Date(endTime));
                return uploadResponse;
            }
        } else if (retCode != 1) {
            uploadResponse.setResult(UploadResponse.ResultStatus.FAILED);
            uploadResponse.setMessage("上传失败，具体原因我也不晓得: " + uploadResp);
            return uploadResponse;
        } else {
            Pic_1 p = uploadResp.getData().getPics().getPic_1();
            ImageInfo imageInfo = new ImageInfoBuilder()
                .setImageInfo(p.getPid(), p.getWidth(), p.getHeight(), p.getSize())
                .build();
            uploadResponse.setResult(UploadResponse.ResultStatus.SUCCESS);
            uploadResponse.setMessage("upload success!");
            uploadResponse.setImageInfo(imageInfo);
            return uploadResponse;
        }
    }


    private synchronized void login() throws IOException, LoginFailedException {
        preLogin();
        String loginUrl = "https://login.sina.com.cn/sso/login.php?client=ssologin.js(v1.4.19)";
        PreLogin preLogin = new Gson().fromJson(preLoginResult, PreLogin.class);

        // 根据微博加密js中密码拼接的方法
        String pwd = preLogin.getServertime() + "\t" + preLogin.getNonce() + "\n" + PASSWORD;

        Map<String, String> params = new HashMap<>(20);
        params.put("encoding", "UTF-8");
        params.put("entry", "weibo");
        params.put("from", "");
        params.put("gateway", "1");
        params.put("nonce", preLogin.getNonce());
        params.put("pagerefer", "https://login.sina.com.cn/crossdomain2.php?action=logout&r=https%3A%2F%2Fweibo.com%2Flogout.php%3Fbackurl%3D%252F");
        params.put("prelt", "76");
        params.put("pwencode", "rsa2");
        params.put("qrcode_flag", "false");
        params.put("returntype", "META");
        params.put("rsakv", preLogin.getRsakv());
        params.put("savestate", "7");
        params.put("servertime", String.valueOf(preLogin.getServertime()));
        params.put("service", "miniblog");
        try {
            params.put("sp", RSAEncodeUtils.encode(pwd, preLogin.getPubkey(), "10001"));
            log.trace("正在登陆...密码加密成功!");
        } catch (NoSuchAlgorithmException | NoSuchPaddingException |
            InvalidKeySpecException | InvalidKeyException |
            IllegalBlockSizeException | BadPaddingException e) {
            log.trace("登陆失败，原因：密码加密失败", new LoginFailedException());
        }
        params.put("sr", "1920*1080");
        params.put("su", Base64.getEncoder().encodeToString(USERNAME.getBytes()));
        params.put("url", "https://weibo.com/ajaxlogin.php?framelogin=1&callback=parent.sinaSSOController.feedBackUrlCallBack");
        params.put("useticket", "1");
        params.put("vsnf", "1");

        WbpHttpResponse wbpHttpResponse = wbpHttpRequest.doPost(loginUrl, getLoginHeader(), params);

        if (wbpHttpResponse.getStatusCode() == HTTP_OK) {

            String cookie = wbpHttpResponse.getHeader().get("set-cookie");
            if (cookie == null) {
                cookie = wbpHttpResponse.getHeader().get("Set-Cookie");
            }
            if (log.isDebugEnabled()) {
                log.trace("login cookie result: \n" + cookie);
            }
            if (cookie == null) {
                throw new LoginFailedException("登陆失败，无法获取cookie");
            }
            if (cookie.length() < 50) {
                throw new LoginFailedException("登陆失败，大概是用户名密码不正确大概是需要输入验证码了。" +
                                               "由于不知道为何读取返回的body时候乱码，无法解决，所以无法具体说出什么原因。");
            }
            log.trace("登陆成功,cookie:--->\n\n" + cookie + "\n");
            log.info("登陆成功！获取cookie成功!");
            // 将重登状态重设为0
            tryLoginCount.set(0);
            // 存入cookie
            CookieContext.getInstance().setCOOKIE(cookie);
        } else {
            throw new LoginFailedException("login failed,reason: " + wbpHttpResponse.getBody());
        }
    }

    private void preLogin() throws IOException, LoginFailedException {
        String username = Base64.getEncoder().encodeToString(USERNAME.getBytes());
        String preLoginUrl = "https://login.sina.com.cn/sso/prelogin.php";
        Map<String, String> params = new HashMap<>(6);
        params.put("client", "ssologin.js(v1.4.19)");
        params.put("entry", "weibo");
        params.put("su", username);
        params.put("rsakt", "mod");
        params.put("checkpin", "1");
        params.put("_", String.valueOf(System.currentTimeMillis()));

        WbpHttpResponse wbpHttpResponse = wbpHttpRequest.doGet(preLoginUrl, getPreLoginHeader(), params);
        if (wbpHttpResponse.getStatusCode() == HTTP_OK) {
            preLoginResult = wbpHttpResponse.getBody();
        }
        if (preLoginResult == null) {
            throw new LoginFailedException("weibo prelogin failed!");
        }
    }

    private String imageToBase64(File imageFile) {
        String base64Image = "";
        try (FileInputStream imageInFile = new FileInputStream(imageFile)) {
            // Reading a Image file from file system
            byte[] imageData = new byte[(int) imageFile.length()];
            int read = imageInFile.read(imageData);
            log.trace("read imageFile: [" + read + "]");
            base64Image = Base64.getEncoder().encodeToString(imageData);
        } catch (FileNotFoundException e) {
            log.trace("Image not found" + e);
        } catch (IOException ioe) {
            log.trace("Exception while reading the Image " + ioe);
        }
        return base64Image;
    }

    private String getFileExtension(String fileName) {
        String extension = null;
        int i = fileName.lastIndexOf(".");
        if (i > 0) {
            extension = fileName.substring(i + 1);
        }
        if (extension == null) {
            return "jpg";
        }
        if (IMAGE_EXTENSION.contains(extension)) {
            return extension;
        }
        return "jpg";
    }

    private void checkLogin() throws IOException, LoginFailedException {
        CookieContext instance = CookieContext.getInstance();
        if (instance.getCOOKIE() == null) {
            login();
        }
    }

    private synchronized boolean tryReLogin() throws IOException, LoginFailedException {
        final long currentTime = System.currentTimeMillis();
        // 先判断是否已经到了冷却时间
        if (endTime != 0 && endTime < currentTime) {
            int i = tryLoginCount.addAndGet(1);
            endTime = System.currentTimeMillis() + tryLoginTime * i;
            login();
            return true;
        }
        return false;
    }

    private Map<String, String> getPreLoginHeader() {
        Map<String, String> header = new HashMap<>(2);
        header.put("Referer", "https://weibo.com/");
        header.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/67.0.3396.79 Safari/537.36");
        return header;
    }

    private Map<String, String> getLoginHeader() {
        Map<String, String> header = new HashMap<>(6);
        header.put("Referer", "https://weibo.com/");
        header.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; …) Gecko/20100101 Firefox/63.0");
        header.put("Content-Type", "application/x-www-form-urlencoded");
        header.put("Accept", "text/html,application/xhtml+xm…plication/xml;q=0.9,*/*;q=0.8");
        header.put("Accept-Encoding", "gzip, deflate, br");
        header.put("Accept-Language", "zh-CN,zh;q=0.8,zh-TW;q=0.7,zh-HK;q=0.5,en-US;q=0.3,en;q=0.2");
        return header;
    }

    private Map<String, String> getUploadHeader() {
        Map<String, String> header = new HashMap<>(4);
        header.put("Host", "picupload.service.weibo.com");
        header.put("Cookie", CookieContext.getInstance().getCOOKIE());
        header.put("Origin", "https://weibo.com/");
        header.put("Referer", "https://weibo.com/");
        return header;
    }
}
