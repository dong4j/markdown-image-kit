package info.dong4j.idea.plugin.sdk.qcloud.cos.auth;

public class COSStaticCredentialsProvider implements COSCredentialsProvider {

    private final COSCredentials cred;

    public COSStaticCredentialsProvider(COSCredentials cred) {
        super();
        this.cred = cred;
    }

    @Override
    public COSCredentials getCredentials() {
        return cred;
    }
}
