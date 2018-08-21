importResource("/admin/css/common.css");

importMiniui(function () {
    mini.parse();
    require(["request","miniui-tools", "search-box"],function (request,tools,SearchBox) {

        new SearchBox({
            container: $("#search-box"),
            onSearch: search,
            initSize:2
        }).init();

        var grid = window.grid = mini.get("datagrid");
        tools.initGrid(grid);

        grid.setUrl(request.basePath + "plugin-permission-group");

        function search() {
            tools.searchGrid("#search-box", grid);
        }

        search();

        window.renderStatus = function (e) {
            var name = '';
            if (e.value == '1') {
                name = '有效';
            } else if (e.value == '2') {
                name = '无效';
            }
            return name;
        }

        window.renderDate = function(e){
            var name = new Date(e.value).toLocaleDateString();
            return name;
        }

        $(".search-button").click(search);
        tools.bindOnEnter("#search-box", search);

        $(".add-button").click(function () {
            tools.openWindow("admin/plugin/permission/group/save.html", "添加分类", "600", "600", function () {
                grid.reload();
            })
        });

        function edit(id) {
            tools.openWindow("admin/plugin/permission/group/save.html?id=" + id, "编辑分类", "600", "600", function () {
                grid.reload();
            })
        }

        window.renderPermission = function (e) {
            var row = e.record;
            var html = [
                tools.createActionLink("详情", "权限详情", function () {
                    var versionForm = mini.get("permission-form");
                    versionForm.showAtPos('center', 'middle');
                    var groupId = row.id;
                    var html = "";
                    $("#content").html("");
                    request.get("plugin-permission-group/permission?id=" + groupId, function (e) {
                        var data = e.result;
                        for (var i = 0; i < data.length; i++) {
                            html += "<div class='mini-col-3' style='margin: 10px 10px 10px 10px;font-size: 16px'>" + data[i].name + "</div>";
                        }
                        $("#content").html(html);

                    });

                })
            ];
            return html.join("");
        }

        window.renderAction = function (e) {
            var row = e.record;
            var html = [];
            if (authorize.hasPermission("plugin-permission-group","update")){
                html.push(tools.createActionButton("编辑", "icon-edit", function () {
                    edit(row.id);
                }));
            };
            if (authorize.hasPermission("plugin-permission-group","delete")){
                html.push(tools.createActionButton("删除", "icon-remove", function () {
                    require(["message"], function (message) {
                        message.confirm("确定删除该种类？", function () {
                            var loading = message.loading("删除中...");
                            request["delete"]("plugin-permission-group/" + row.id, {}, function (res) {
                                loading.close();
                                if (res.status == 200) {
                                    // console.log(res.status);
                                    // e.sender.removeNode(row);
                                    grid.reload();
                                } else {
                                    message.showTips("删除失败" + res.message);
                                }
                            })
                        })
                    })
                }));
            };

            return html.join("");
        }

    });
});