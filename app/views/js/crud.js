

function newTypeOfBoard(){
    model_data = $("#newTypeOfBoard").serializeObject();
    $.ajax({
        url: '@routes.CompilationLibrariesController.new_TypeOfBoard()',
        type: 'POST',
        contentType: 'application/json',
        headers: {
            "X-AUTH-TOKEN":getCookie("authToken"),
            "Content-Type":"application/json"
        },
        data: JSON.stringify(model_data),
        dataType: 'json',
        success:function(e){
            $("#newTypeOfBoard input[type=text]").val("");
            $('#allTypesOfBoards').load(document.URL +  ' #allTypesOfBoards');
        },
        error:function(e) {
            alert("Unsuccessful. Check if all properties are correct");
        }
    })}

function editTypeOfBoard(id){
    model_data = $("#editTypeOfBoard").serializeObject();

    $.ajax({
        url: "@serverAddress" + "/compilation/typeOfBoard/" + id ,
        type: 'PUT',
        contentType: 'application/json',
        headers: {
            "X-AUTH-TOKEN":getCookie("authToken"),
            "Content-Type":"application/json"
        },
        data: JSON.stringify(model_data),
        dataType: 'json',
        success:function(e){
            $("#editTypeOfBoard input[type=text]").val("");
            $("#editTypeOfBoardModal").css("display","none");
            $('#allTypesOfBoards').load(document.URL +  ' #allTypesOfBoards');
        },
        error:function(e) {
            alert("Unsuccessful. Check if all properties are correct");
        }
    })}

function deleteTypeOfBoard(id){

    $.ajax({
        url: "@serverAddress" + "/compilation/typeOfBoard/" + id ,
        type: 'DELETE',
        headers: {
            "X-AUTH-TOKEN":getCookie("authToken"),
        },
        success:function(e){
            $('#allTypesOfBoards').load(document.URL +  ' #allTypesOfBoards');
        },
        error:function(e) {
            alert("Unsuccessful. Maybe it cannot be deleted, because there are some boards of this type.");
        }
    })}

function showEditForm(id, typeOfBoardName, description, producer_id, processor_id, checkBoxValue){
    $("#editTypeOfBoardModal").css("display","block");
    $('#editTypeOfBoardButton').val(id);
    $('#typeOfBoardId').text("ID = " + id);
    $("#editTypeOfBoard input[name=name]").val(typeOfBoardName);
    $("#editTypeOfBoard input[name=description]").val(description);
    $("#editTypeOfBoard input[name=producer_id]").val(producer_id);
    $("#editTypeOfBoard input[name=processor_id]").val(processor_id);
    $("#editTypeOfBoard input[name=connectible_to_internet]").prop( "checked", checkBoxValue );
}



function newProcessor(){
    model_data = $("#newProcessor").serializeObject();
    $.ajax({
        url: '@routes.CompilationLibrariesController.new_Processor()',
        type: 'POST',
        contentType: 'application/json',
        headers: {
            "X-AUTH-TOKEN":getCookie("authToken"),
            "Content-Type":"application/json"
        },
        data: JSON.stringify(model_data),
        dataType: 'json',
        success:function(e){
            $("#newProcessor input[type=text]").val("");
            $('#allProcessor').load(document.URL +  ' #allProcessors');
        },
        error:function(e) {
            alert("Unsuccessful. Check if all properties are correct");
        }
    })}

function editProcessor(id){
    model_data = $("#editProcessor").serializeObject();

    $.ajax({
        url: "@serverAddress" + "/compilation/processor/" + id ,
        type: 'PUT',
        contentType: 'application/json',
        headers: {
            "X-AUTH-TOKEN":getCookie("authToken"),
            "Content-Type":"application/json"
        },
        data: JSON.stringify(model_data),
        dataType: 'json',
        success:function(e){
            $("#editTProcessor input[type=text]").val("");
            $("#editProcessorModal").css("display","none");
            $('#allProcessor').load(document.URL +  ' #allProcessors');
        },
        error:function(e) {
            alert("Unsuccessful. Check if all properties are correct");
        }
    })}

function deleteProcessor(id){

    $.ajax({
        url: "@serverAddress" + "/compilation/processor/" + id ,
        type: 'DELETE',
        headers: {
            "X-AUTH-TOKEN":getCookie("authToken"),
        },
        success:function(e){
            $('#allProcessors').load(document.URL +  ' #allProcessors');
        },
        error:function(e) {
            alert("Unsuccessful. Maybe it cannot be deleted, because there are some boards connected on this processor");
        }
    })}

function showEditFormProcessor(id, processor_name, description, processor_code, speed){
    $("#editProcessorModal").css("display","block");
    $('#editProcessorButton').val(id);
    $('#processorId').text("ID = " + id);
    $("#editProcessor input[name=name]").val(processor_name);
    $("#editProcessor input[name=description]").val(description);
    $("#editProcessor input[name=producer_id]").val(processor_code);
    $("#editProcessor input[name=processor_id]").val(speed);
}

