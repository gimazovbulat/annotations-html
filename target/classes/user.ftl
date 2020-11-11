<form method=${method} action=${action}>
    <#list formInputs as input>
        <input type=${input.type} name=${input.name} placeholder=${input.placeholder}>
    </#list>
</form>