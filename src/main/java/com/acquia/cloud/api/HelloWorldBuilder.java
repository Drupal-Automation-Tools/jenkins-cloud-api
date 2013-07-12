package com.acquia.cloud.api;
import hudson.Launcher;
import hudson.Extension;
import hudson.util.FormValidation;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.AbstractProject;
import hudson.tasks.Builder;
import hudson.tasks.BuildStepDescriptor;
import net.sf.json.JSONObject;
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
public class HelloWorldBuilder extends Builder {

    private final String name;
    
    public String user, pass, sites, env;
    public boolean deploy;
    
    // Fields in config.jelly must match the parameter names in the "DataBoundConstructor"
    @DataBoundConstructor
    public HelloWorldBuilder(String name, String user, String pass, boolean deploy, String env) {
        this.name = name;
        this.user = user;
        this.pass = pass;
        this.deploy = deploy;
        this.env = env;
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
    
    @Override
    public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) {
       //This is the main routine
      
       //Create an new client connection via the lib
       //@TODO: Move listener to handle system.out error trapping
      
       CloudAPIClient client = new CloudAPIClient(this.user, this.pass);

       //Implements the getSites meathod 
       //@TODO: move listener to object implementation
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
            listener.getLogger().println("Requesting a code deployment.");
            HashMap deployResult = client.vcsDeployVcsEnv(sites, "dev", "master");
            listener.getLogger().println("TASK:"+deployResult.get("id").toString()+" created! ");
       } else { 
       // @TODO: This is done! Party! And Remove it ... this is only here for testing.  
            listener.getLogger().println("Not requesting a code deployment.");       
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
     * Descriptor for {@link HelloWorldBuilder}. Used as a singleton.
     * The class is marked as public so that it can be accessed from views.
     *
     * <p>
     * See <tt>src/main/resources/hudson/plugins/hello_world/HelloWorldBuilder/*.jelly</tt>
     * for the actual HTML fragment for the configuration screen.
     */
    @Extension // This indicates to Jenkins that this is an implementation of an extension point.
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {
        /**
         * To persist global configuration information,
         * simply store it in a field and call save().
         *
         * <p>
         * If you don't want fields to be persisted, use <tt>transient</tt>.
         */
        private boolean useFrench;

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
            return "Say query the site name in Acquia Cloud API to the PSO-peeps and Mr.Miles !";
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            // To persist global configuration information,
            // set that to properties and call save().
            useFrench = formData.getBoolean("useFrench");
            // ^Can also use req.bindJSON(this, formData);
            //  (easier when there are many fields; need set* methods for this, like setUseFrench)
            save();
            return super.configure(req,formData);
        }

        /**
         * This method returns true if the global configuration says we should speak French.
         *
         * The method name is bit awkward because global.jelly calls this method to determine
         * the initial state of the checkbox by the naming convention.
         */
        public boolean getUseFrench() {
            return useFrench;
        }
    }
}
