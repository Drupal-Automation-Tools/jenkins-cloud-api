package com.acquia.cloud.api;
import hudson.Launcher;
import hudson.Extension;
import hudson.util.FormValidation;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.AbstractProject;
import hudson.tasks.Builder;
import hudson.tasks.BuildStepDescriptor;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.QueryParameter;
import javax.servlet.ServletException;
import java.io.IOException;
import java.util.HashMap;

/**
 * Sample {@link Builder}.
 *
 * <p>
 * When the user configures the project and enables this builder,
 * {@link DescriptorImpl#newInstance(StaplerRequest)} is invoked
 * and a new {@link HelloWorldBuilder} is created. The created
 * instance is persisted to the project configuration XML by using
 * XStream, so this allows you to use instance fields (like {@link #name})
 * to remember the configuration.
 *
 * <p>
 * When a build is performed, the {@link #perform(AbstractBuild, Launcher, BuildListener)}
 * method will be invoked. 
 *
 * @author Kohsuke Kawaguchi
 */
public class DeployCodeBuilder extends Builder {

    private final String name;
    
    public String user, pass, sites, env, tag;
    public boolean deploy;
    
    // Fields in config.jelly must match the parameter names in the "DataBoundConstructor"
    @DataBoundConstructor
    public DeployCodeBuilder(String name, String user, String pass, boolean deploy, String env, String tag) {
        this.name = name;
        this.user = user;
        this.pass = pass;
        this.deploy = deploy;
        this.env = env;
        this.tag = tag;
    }

    /**
     * We'll use this from the <tt>config.jelly</tt>.
     */
    public String getName() {
        return name;
    }

    public String getUser() {
        return user;
    }

    public String getPass() {
        return pass;
    }

    public boolean getDeploy() {
       return deploy;
    }    

    public String getEnv() {
       return env;
    }    
    
    public String getTag() {
       return tag;
    }    
    
    @Override
    public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) throws IOException {
       //This is the main routine
      
       //Create an new client connection via the lib
       //@TODO: Move listener to handle system.out error trapping
      
       CloudAPIClient client = new CloudAPIClient(this.user, this.pass);

       //Implements the getSites meathod 
       sites = client.getSites(listener);
       
       //Output task description 
       //@TODO: Maybe remove the freetext and standardize 
       listener.getLogger().println(name);
       
       //@TODO: Error Trap
       listener.getLogger().println("Hello, "+sites+" is the name of the site you called!");
       
       // Implement vcsDeployVcsEnv(String sitename, String envname, String tagname)
       // if they want a code deploy we need to have the tag/branch pushed as an arg
       // @TODO: Lotsa Shit
       // @TODO: Error trapping
       // @TODO: Pass in Branch/Tag as an argument from a previous step? Can this be a config.jelly?
       // @TODO: Select Environment? Should this be a text field or dynamic?
       // @TODO: Store Task ID and state
       // @TODO: Create Callback to check Task Completion state
       // @TODO: If a task fails do we redeploy or fail?

       if (deploy) {
            listener.getLogger().println("Requesting a code deployment of "+tag+" to "+sites+" Env:"+env+".");
            HashMap deployResult = client.vcsDeployVcsEnv(sites, env, tag);
            listener.getLogger().println("TASK:"+deployResult.get("id").toString()+" created! ");
       }      
       return true;
    }

    // Overridden for better type safety.
    // If your plugin doesn't really define any property on Descriptor,
    // you don't have to do this.
    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl)super.getDescriptor();
    }

    /**
     * Descriptor for {@link DeployCodeBuilder}. Used as a singleton.
     * The class is marked as public so that it can be accessed from views.
     *
     * <p>
     * See <tt>src/main/resources/hudson/plugins/hello_world/HelloWorldBuilder/*.jelly</tt>
     * for the actual HTML fragment for the configuration screen.
     */
    @Extension // This indicates to Jenkins that this is an implementation of an extension point.
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {
        /**
         * Performs on-the-fly validation of the form field 'name'.
         *
         * @param value
         *      This parameter receives the value that the user has typed.
         * @return
         *      Indicates the outcome of the validation. This is sent to the browser.
         */
        public FormValidation doCheckName(@QueryParameter String value)
                throws IOException, ServletException {
            if (value.length() == 0)
                return FormValidation.error("Please set a name");
            if (value.length() < 4)
                return FormValidation.warning("Isn't the name too short?");
            return FormValidation.ok();
        }

        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            // Indicates that this builder can be used with all kinds of project types 
            return true;
        }

        /**
         * This human readable name is used in the configuration screen.
         */
        public String getDisplayName() {
            return "Deploy a Branch/Tag to an Environment";
        }
    }
}

