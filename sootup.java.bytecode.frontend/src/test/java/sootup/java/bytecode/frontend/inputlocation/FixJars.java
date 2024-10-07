package sootup.java.bytecode.frontend.inputlocation;

import categories.TestCategories;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import sootup.java.core.views.JavaView;

@Tag(TestCategories.JAVA_8_CATEGORY)
public class FixJars extends BaseFixJarsTest {

@Test
public void executeUtilitiesjar(){
	String jarDownloadUrl = "https://repo1.maven.org/maven2/com/dorkbox/Utilities/1.48/Utilities-1.48.jar";
    String methodSignature = "<dorkbox.util.FileUtil: java.util.List readLines(java.io.Reader)>";
    JavaView javaView = supplyJavaView(jarDownloadUrl);
    assertMethodConversion(javaView,methodSignature);
    assertJar(javaView);
}

}