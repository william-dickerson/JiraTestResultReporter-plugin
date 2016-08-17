package org.jenkinsci.plugins.JiraTestResultReporter.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import hudson.matrix.MatrixProject;
import hudson.model.Api;
import hudson.model.Job;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.JiraTestResultReporter.TestToIssueMapping;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by tuicu on 12/08/16.
 */
public class TestToIssueMappingApi extends Api {
    private static final Gson GSON = new GsonBuilder()
            .disableHtmlEscaping()
            .setPrettyPrinting()
            .create();

    public Api getApi() {
        return this;
    }

    public TestToIssueMappingApi() {
        super(null);
    }

    @Override
    public void doJson(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
        String jobName = req.getParameter("job");
        JsonElement result;

        if(jobName == null) {
            rsp.getWriter().write("You need to set the \"job\" parameter");
            return;
        }

        //sub job of a matrix project
        if(jobName.contains("/")) {
            String matrixJobName = jobName.split("/")[0];
            String matrixSubJobName = jobName.split("/")[1];
            MatrixProject matrixProject = (MatrixProject) Jenkins.getActiveInstance().getItem(matrixJobName);
            if(matrixProject == null) {
                rsp.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            result = TestToIssueMapping.getInstance().getMap(matrixProject, matrixSubJobName);
        // top level job (either matrix, freestyle or maven
        } else {
            Job job = (Job) Jenkins.getActiveInstance().getItem(jobName);
            if (job == null) {
                rsp.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            result = TestToIssueMapping.getInstance().getMap(job);
        }

        if(result != null) {
            rsp.setContentType("application/json");
            rsp.getWriter().write(GSON.toJson(result));
        } else {
            rsp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }
}
