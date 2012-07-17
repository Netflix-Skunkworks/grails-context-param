# Grails context param plugin

In some cases, you may want to ensure a specific context is always passed along to a Grails controllers. For example, you may want to make sure a RESTful url always includes a region for certain controllers. Your URLMappings.groovy will have an entry like:

`/$region/$controller/$action?/$id?`

This requires passing the region parameter through every request if you want avoid keeping that state in the session. In practice, this means that all of the redirect and chain blocks will have to repeat appending the same parameters to every call. Your code will have lines like these scattered all over:

`redirect(action: show, params: [region: region])`

or

`chain(action: create, model: [cmd: cmd], params: [region: region])`

This plugin allows you to annotate controllers with `@ContextParam` annotations to specify these parameters and automatically include them on any redirect or chain calls. 

For example:
```
@ContextParam('region') 
class InstanceController {
	...
```
will append the `region` param from the current request if `redirect(controller: 'instance')` is called, eliminating the need to append the params to the call.

In addition, the configuration of context param declarations is available on the `grailsApplication` object in the field `Map<String,List<String>> controllerNamesToContextParams`. To check if region is a `@ContextParam` for a particular controller you can call:

`grailsApplication.controllerNamesToContextParams[(controllerName)].contains('region')`

This plugin will be used by [Netflix's Asgard project](https://github.com/Netflix/asgard) for it's upgrade to Grails 2.1.