package sootup.java.bytecode.frontend.inputlocation;

import categories.TestCategories;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import sootup.java.core.views.JavaView;

@Tag(TestCategories.JAVA_8_CATEGORY)
public class FixJars extends BaseFixJarsTest {

@Test
public void executebrowserstackjavaagentjar(){
	String jarDownloadUrl = "https://repo1.maven.org/maven2/com/browserstack/browserstack-javaagent/1.0.0/browserstack-javaagent-1.0.0.jar";
    String methodSignature = "<junit.framework.TestCase: void runBare()>";
    JavaView javaView = supplyJavaView(jarDownloadUrl);
    assertMethodConversion(javaView,methodSignature);
    assertJar(javaView);
}

@Test
public void executecordanodeapiDevPreviewjar(){
	String jarDownloadUrl = "https://repo1.maven.org/maven2/net/corda/corda-node-api/5.0.0-DevPreview/corda-node-api-5.0.0-DevPreview.jar";
    String methodSignature = "<net.corda.nodeapi.internal.protonwrapper.engine.ConnectionStateMachine: void transportProcessInput(io.netty.buffer.ByteBuf)>";
    JavaView javaView = supplyJavaView(jarDownloadUrl);
    assertMethodConversion(javaView,methodSignature);
    assertJar(javaView);
}

}