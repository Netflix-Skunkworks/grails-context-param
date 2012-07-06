# Grails context param plugin

In some cases, you may want to ensure a specific context is always passed along to a Grails controllers. For example, you may want to make sure you're RESTful url always includes a region for certain controllers. This leads to replicated code for adding that parameter to every redirect or chain call into the controller. 

This plugin allows you to annotate controllers with `@ContextParam` annotations to specify these parameters and automatically include them on any redirect or chain calls.

For example:
```
@ContextParam('region') 
class InstanceController {
	...
```
will append the `region` param from the current request if `redirect(controller: 'instance')` is called.

In addition, the configuration of context param declarations is available on the `grailsApplication` object in the field `Map<String,List<String>> controllerNamesToContextParams`.