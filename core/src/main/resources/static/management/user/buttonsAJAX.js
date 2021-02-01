function Order() {
    var allParam = window.location.search;
    var urlSearch = new URLSearchParams(allParam);
    var sort = urlSearch.get("sort");
    if (sort !== null) {
        if (sort.includes('id')) {
            if (sort.includes('ASC')) {
                document.getElementById("id-icon").className = 'fas fa-chevron-up';
            } else {
                document.getElementById("id-icon").className = "fas fa-chevron-down";
            }
        } else if (sort.includes('name')) {
            if (sort.includes('ASC')) {
                document.getElementById("user-icon").className = 'fas fa-chevron-up';
            } else {
                document.getElementById("user-icon").className = "fas fa-chevron-down";
            }
        } else if (sort.includes('email')) {
            if (sort.includes('ASC')) {
                document.getElementById("email-icon").className = 'fas fa-chevron-up';
            } else {
                document.getElementById("email-icon").className = "fas fa-chevron-down";
            }
        } else if (sort.includes('userCredo')) {
            if (sort.includes('ASC')) {
                document.getElementById("credo-icon").className = 'fas fa-chevron-up';
            } else {
                document.getElementById("credo-icon").className = "fas fa-chevron-down";
            }
        } else if (sort.includes('role')) {
            if (sort.includes('ASC')) {
                document.getElementById("role-icon").className = 'fas fa-chevron-up';
            } else {
                document.getElementById("role-icon").className = "fas fa-chevron-down";
            }
        } else if (sort.includes('userStatus')) {
            if (sort.includes('ASC')) {
                document.getElementById("tags-icon").className = 'fas fa-chevron-up';
            } else {
                document.getElementById("tags-icon").className = "fas fa-chevron-down";
            }
        }

    }
}


function orderByNameField(nameField) {
    var allParam = window.location.search;
    var urlSearch = new URLSearchParams(allParam);
    var sort = urlSearch.get("sort");
    var page = urlSearch.get("page");
    if (page !== null) {
        urlSearch.set("page", "0");
    }
    if (sort == null) {
        urlSearch.set("sort", nameField + ",ASC");
    } else if (sort.includes(nameField)) {
        sort = sort.toUpperCase();
        if (sort.includes("ASC")) {
            urlSearch.set("sort", nameField + ",DESC");
        } else if (sort.includes("DESC")) {
            urlSearch.set("sort", nameField + ',ASC');
        }
    } else {
        urlSearch.set("sort", nameField + ",ASC");
    }

    let url = "/management/users?";
    $.ajax({
        url: url + urlSearch.toString(),
        type: 'GET',
        success: function (res) {
            window.location.href = url + urlSearch.toString();
        }
    });
}

function otherCheck() {
    let other = document.getElementById('other');
    if (other.checked == true) {
        document.getElementById("othertext").disabled = false;
    } else {
        document.getElementById("othertext").disabled = true;
    }
};

function clearAllErrorsSpan() {
    $('.errorSpan').text('');
}

let checkedCh = 0;

function updateCheckBoxCount(chInt) {
    let chBox = $('#checkbox' + chInt);
    let deactivateButton = $("#btnDeactivate");
    chBox.is(":checked") ? checkedCh++ : checkedCh--;
    console.log(checkedCh)
    if (checkedCh === 0) {
        deactivateButton.addClass("disabled");
    } else deactivateButton.removeClass("disabled");
}

$(document).ready(function () {
    let deactivateButton = $("#btnDeactivate");

    // Activate tooltip
    $('[data-toggle="tooltip"]').tooltip();

    // Select/Deselect checkboxes
    var checkbox = $('table tbody input[type="checkbox"]');
    $("#selectAll").click(function () {
        if (this.checked) {
            checkedCh = 0;
            checkbox.each(function () {
                this.checked = true;
                checkedCh++;
            });
            deactivateButton.removeClass("disabled");
        } else {
            checkbox.each(function () {
                checkedCh--;
                this.checked = false;
            });
            deactivateButton.addClass("disabled");
        }
    });
    checkbox.click(function () {
        if (!this.checked) {
            $("#selectAll").prop("checked", false);
        }
    });

    $('#btnSearchImage').click(function () {
        let url = "/management/users?query=";
        let query = $('#inputSearch').val();
        $.ajax({
            url: url + query,
            type: 'GET',
            success: function (res) {
                window.location.href = url + query;
            }
        });
    });


    // Add user button (popup)
    $('#addUserModalBtn').on('click', function (event) {
        clearAllErrorsSpan();
    });

    // Submit button in addUserModal
    $('#submitAddBtn').on('click', function (event) {
        event.preventDefault();
        clearAllErrorsSpan();
        var formData = $('#addUserForm').serializeArray().reduce(function (obj, item) {
            obj[item.name] = item.value;
            return obj;
        }, {});
        var payload = {
            "id": formData.id,
            "name": formData.name,
            "email": formData.email,
            "role": formData.role,
            "userStatus": formData.userStatus
        }

        // Ajax request
        $.ajax({
            url: '/management/users/register',
            type: 'post',
            dataType: 'json',
            contentType: 'application/json',
            success: function (data) {
                if (Array.isArray(data.errors) && data.errors.length) {
                    data.errors.forEach(function (el) {
                        $(document.getElementById('errorModalSave' + el.fieldName)).text(el.fieldError);
                    })
                } else {
                    location.reload();
                }
            },
            data: JSON.stringify(payload)
        });
    })

    // Edit user button (popup)
    $('td .edit.eBtn').on('click', function (event) {
        event.preventDefault();
        $("#editUserModal").each(function () {
            $(this).find('input.eEdit').val("");
        });
        clearAllErrorsSpan();
        $('#editUserModal').modal();
        var href = $(this).attr('href');
        $.get(href, function (user, status) {
            $('#id').val(user.id);
            $('#name').val(user.name);
            $('#email').val(user.email);
            $('#role').val(user.role);
            $('#userCredo').val(user.userCredo);
            $('#userStatus').val(user.userStatus);
        });
    });
    // Save editing button in editUserModal
    $('#submitEditBtn').on('click', function (event) {
        event.preventDefault();
        clearAllErrorsSpan();
        var formData = $('#editUserForm').serializeArray().reduce(function (obj, item) {
            obj[item.name] = item.value;
            return obj;
        }, {});
        var returnData = {
            "id": formData.id,
            "name": formData.name,
            "email": formData.email,
            "role": formData.role,
            "userCredo": formData.userCredo,
            "userStatus": formData.userStatus
        }
        // put request when 'Save' button in editUserModal clicked
        $.ajax({
            url: '/management/users/',
            type: 'put',
            dataType: 'json',
            contentType: 'application/json',
            success: function (data) {
                if (Array.isArray(data.errors) && data.errors.length) {
                    data.errors.forEach(function (el) {
                        $(document.getElementById('errorModalUpdate' + el.fieldName)).text(el.fieldError);
                    })
                } else {
                    location.reload();
                }
            },
            data: JSON.stringify(returnData)
        });
    })

    // Deactivate user button (popup)
    $('td .deactivate-user.eDeactBtn').on('click', function (event) {
        event.preventDefault();
        $('#deactivateUserModal').modal();
        var href = $(this).attr('href');
        $('#deactivateOneSubmit').attr('href', href);
    });
    // Confirm deactivation button in deactivateUserModal
    $('#deactivateOneSubmit').on('click', function (event) {
        event.preventDefault();
        var href = $(this).attr('href');
        $.ajax({
            url: href,
            type: 'post',
            success: function (data) {
                location.reload();
            },
        });
    });

    // Deactivate user button (popup)
    $('td .activate-user.eActBtn').on('click', function (event) {
        event.preventDefault();
        $('#activateUserModal').modal();
        var href = $(this).attr('href');
        $('#activateOneSubmit').attr('href', href);
    });
    // Confirm deactivation button in activateUserModal
    $('#activateOneSubmit').on('click', function (event) {
        event.preventDefault();
        var href = $(this).attr('href');
        $.ajax({
            url: href,
            type: 'post',
            success: function (data) {
                location.reload();
            },
        });
    });

    // Deactivate marked users button (popup)
    $('#deactivateAllSubmit').on('click', function (event) {
        event.preventDefault();
        var checkbox = $('table tbody input[type="checkbox"]');
        var payload = [];
        checkbox.each(function () {
            if (this.checked) {
                payload.push(this.value);
            }
        })
        var href = '/management/users/deactivateAll';
        // post request when 'Deactivate marked' button in deactivateAllSelectedModal clicked
        $.ajax({
            url: href,
            type: 'post',
            dataType: 'json',
            contentType: 'application/json',
            success: function (data) {
                location.reload();
            },
            data: JSON.stringify(payload)
        });
    });

    $(".clickable-row").click(function () {
        $('#userFriendsModal').modal();
        $('.modal-content').scrollTop(0);
        const href = $(this).attr("href");
        const body = $('#friendsTable').find('tbody');
        body.html("");
        $.get(href, function (users) {
            for (const user of users) {
                body.append($('<tr>')
                    .append($('<td>').text(user.id))
                    .append($('<td>').text(user.name))
                    .append($('<td>').text(user.email))
                    .append($('<td>').text(user.userCredo))
                    .append($('<td>').text(user.role))
                    .append($('<td>').text(user.userStatus)));
            }
        })
    });
});