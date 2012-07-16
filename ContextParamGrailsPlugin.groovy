import com.netflix.grails.contextParam.ContextParam
import org.codehaus.groovy.grails.commons.ControllerArtefactHandler
import org.codehaus.groovy.grails.commons.GrailsControllerClass
import org.springframework.web.context.request.RequestContextHolder

class ContextParamGrailsPlugin {

    def observe = ["controllers"]

    def version = "1.0"
    def grailsVersion = "1.3.7 > *"
    def loadAfter = ['controllers']

    def author = "Jason Gritman"
    def authorEmail = "jgritman@netflix.com"
    def title = "Context param plugin"
    def description = 'Automatically adds parameters specified as @ContextParam on a controller to redirect calls.'
    def documentation = "http://grails.org/plugin/context-param"

    // License: one of 'APACHE', 'GPL2', 'GPL3'
    def license = 'APACHE'

    // Details of company behind the plugin (if there is one)
    // def organization = [ name: "My Company", url: "http://www.my-company.com/" ]

    // Any additional developers beyond the author specified above.
    // def developers = [ [ name: "Joe Bloggs", email: "joe@bloggs.net" ]]

    // Location of the plugin's issue tracker.
    // def issueManagement = [ system: "JIRA", url: "http://jira.grails.org/browse/GPMYPLUGIN" ]

    def scm = [url: 'https://github.com/Netflix-Skunkworks/grails-context-param']
 
    def doWithDynamicMethods = { ctx ->
        Map<String, List<String>> controllerNamesToContextParams = [:]
        for (GrailsControllerClass controllerClass in application.controllerClasses) {
            Collection<String> contextParams = findContextParams(controllerClass)
            replaceRedirectMethod(controllerClass, contextParams)
            controllerNamesToContextParams[(controllerClass.logicalPropertyName)] = contextParams
        }
        application.metaClass.getControllerNamesToContextParams = { ->
            controllerNamesToContextParams
        }
    }

    def onChange = { event -> 
        // Replace the redirect/chain methods again if the controller has changed
        if(event.source instanceof Class &&
                application.isArtefactOfType(ControllerArtefactHandler.TYPE, event.source)) {
            GrailsControllerClass controllerClass = application.getArtefact(ControllerArtefactHandler.TYPE, 
                    event.source.name)
            Collection<String> contextParams = findContextParams(controllerClass)
            replaceRedirectMethod(controllerClass, contextParams)
            application.controllerNamesToContextParams[(controllerClass.logicalPropertyName)] = contextParams
        }
    }

    /**
     * Wraps the chain and redirect methods.
     */ 
    private void replaceRedirectMethod(GrailsControllerClass controllerClass, Collection<String> contextParams) {
        wrapMethod(controllerClass, contextParams, 'redirect')
        wrapMethod(controllerClass, contextParams, 'chain')
    }

    /**
     * Take a method by name, and add a call to appendContextParams before that method invokation.
     */
    private void wrapMethod(GrailsControllerClass controllerClass, Collection<String> contextParams, String name) {
        def oldMethod = controllerClass.metaClass.pickMethod(name, [Map] as Class[])
        controllerClass.metaClass."${name}" = { Map args ->
            appendContextParams(contextParams, args)
            oldMethod.invoke(delegate, args)
        }
    }

    /**
     * Add any specified context params to the params on the arguments. If there is no params object it will be created.
     */
    private void appendContextParams(Collection<String> contextParams, Map arguments) {
        if (arguments && contextParams ) {
            // If redirect was called without a params map object then make an empty params map.
            Map params = arguments.params ?: [:]
            def request = RequestContextHolder.currentRequestAttributes()
            contextParams.each { String contextParam ->
                if (!params[(contextParam)]) { 
                    params[(contextParam)] = request.params[(contextParam)]
                }
            }
            arguments.params = params
        }
    }

    /**
     * Find all of the @ContextParam annotions on the controller.
     */
    private Collection<String> findContextParams(GrailsControllerClass controllerClass) {
        controllerClass.clazz.declaredAnnotations.findAll { it instanceof ContextParam }.collect { it.value() }
    }
}
