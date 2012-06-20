import com.netflix.grails.contextParam.ContextParam
import org.codehaus.groovy.grails.commons.ControllerArtefactHandler
import org.codehaus.groovy.grails.commons.GrailsControllerClass
import org.springframework.web.context.request.RequestContextHolder

class ContextParamGrailsPlugin {

    def observe = ["controllers"]

    // the plugin version
    def version = "1.0"
    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "1.3.7 > *"
    def loadAfter = ['controllers']

    def author = "Jason Gritman"
    def authorEmail = "jgritman@netflix.com"
    def title = "Plugin summary/headline"
    def description = '''\\
Brief description of the plugin.
'''

    // URL to the plugin's documentation
    def documentation = "http://grails.org/plugin/context-param"

    def doWithDynamicMethods = { ctx ->
        Map<String, List<String>> controllerNamesToContextParams = [:]
        for (GrailsControllerClass controllerClass in application.controllerClasses) {
            Collection<String> contextParams = findContextParams(controllerClass)
            replaceRedirectMethod(controllerClass, contextParams)
            controllerNamesToContextParams[(controllerClass.logicalPropertyName)] = contextParams
        }
        application.metaClass.getControllerNamesToContextParams {
            controllerNamesToContextParams
        }
    }

    def onChange = { event -> 
        if(event.source instanceof Class &&
                application.isArtefactOfType(ControllerArtefactHandler.TYPE, event.source)) {
            GrailsControllerClass controllerClass = application.getArtefact(ControllerArtefactHandler.TYPE, 
                    event.source.name)
            Collection<String> contextParams = findContextParams(controllerClass)
            replaceRedirectMethod(controllerClass, contextParams)
            application.controllerNamesToContextParams[(controllerClass.logicalPropertyName)] = contextParams
        }
    }

    private void replaceRedirectMethod(GrailsControllerClass controllerClass, Collection<String> contextParams) {
        wrapMethod(controllerClass, contextParams, 'redirect')
        wrapMethod(controllerClass, contextParams, 'chain')
    }

    private void wrapMethod(GrailsControllerClass controllerClass, Collection<String> contextParams, String name) {
        def oldMethod = controllerClass.metaClass.pickMethod(name, [Map] as Class[])
        controllerClass.metaClass."${name}" = { Map args ->
            appendContextParams(contextParams, args)
            oldMethod.invoke(delegate, args)
        }
    }

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

    private Collection<String> findContextParams(GrailsControllerClass controllerClass) {
        controllerClass.clazz.declaredAnnotations.findAll { it instanceof ContextParam }.collect { it.value() }
    }
}
