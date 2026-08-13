package com.vkailabs.insurance.automation.runners;

import org.testng.annotations.DataProvider;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;

/**
 * TestNG entry point for the Cucumber suite.
 *
 * <p>Currently {@code tags = "@Client and not @Manual"} runs the automatable client-side
 * scenarios (auth, enrollment, premium, and the automatable E2E slice) while excluding
 * the manual/exploratory legs kept in the feature files for traceability. As more modules
 * are ported, refine the tag filter (or add tag-specific runners) and flip the
 * data-provider to parallel via testng.xml.
 */
@CucumberOptions(
        features = "src/test/resources/features",
        glue = {
                "com.vkailabs.insurance.automation.stepdefinitions",
                "com.vkailabs.insurance.automation.hooks"
        },
        plugin = {
                "pretty",
                "html:target/cucumber-reports/cucumber.html",
                "json:target/cucumber-reports/cucumber.json",
                "com.aventstack.extentreports.cucumber.adapter.ExtentCucumberAdapter:"
        },
        monochrome = true,
        tags = "@Client and not @Manual"
)
public class RegressionRunner extends AbstractTestNGCucumberTests {

    @Override
    @DataProvider(parallel = false)
    public Object[][] scenarios() {
        return super.scenarios();
    }
}
