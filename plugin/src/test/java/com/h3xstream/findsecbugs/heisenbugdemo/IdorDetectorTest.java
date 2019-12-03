package com.h3xstream.findsecbugs.heisenbugdemo;

import com.h3xstream.findbugs.test.BaseDetectorTest;
import com.h3xstream.findbugs.test.EasyBugReporter;
import org.testng.annotations.Test;

import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

public class IdorDetectorTest extends BaseDetectorTest {


    @Test
    public void detectIdor() throws Exception {
        //Locate test code
        String[] files = {
                getClassFilePath("testcode/heisenbugdemo/VulnerableSpringEndpoint")
        };

        //Run the analysis
        EasyBugReporter reporter = spy(new EasyBugReporter());
        analyze(files, reporter);

        //Assertions
        verify(reporter)
                .doReportBug(
                    bugDefinition()
                        .bugType("IDOR")
                        .inClass("VulnerableSpringEndpoint").inMethod("idor")
                        .build()
        );
        }

}
