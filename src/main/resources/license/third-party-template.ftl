Elius Virtual Operator
Copyright 2023-2026 Elia Milioni

===========================================================================
THIRD-PARTY LIBRARIES / NOTICES
===========================================================================

This product bundles or incorporates components from the following third-party projects:

<#if dependencyMap?? && (dependencyMap?size > 0)>
<#list dependencyMap as e>
  <#assign dep = e.getKey()/>
  <#assign licenses = e.getValue()/>
  - ${(dep.name)!(dep.artifactId)} (${dep.groupId}:${dep.artifactId}:${dep.version})
    License: <#list licenses as license><#if license?is_string>${license}<#else>${(license.name)!"Unknown"}</#if><#if license_has_next>, </#if></#list>

</#list>
<#else>
No third-party dependencies found.
</#if>