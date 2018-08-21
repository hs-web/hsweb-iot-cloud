importResource("/admin/css/common.css");

importMiniui(function () {
    mini.parse();
    require(["request","miniui-tools"],function (request,tools) {

        var grid = window.grid = mini.get("datagrid");
        tools.initGrid(grid);

        grid.setUrl(request.basePath+"plugin-version");
        function search() {
            tools.searchGrid("#search-box", grid);
        }
        search();

        $(".search-button").click(search);
        tools.bindOnEnter("#search-box",search);

        $(".add-button").click(function () {
            tools.openWindow("admin/plugin/version/save.html","添加版本","1200","1000",function () {
                grid.reload();
            })
        });

        function edit(id){
            tools.openWindow("admin/plugin/version/save.html?id="+id,"添加版本","800","600",function () {
                grid.reload();
            })
        }


        window.renderStatus = function (e) {
            var name = '';
            if (e.value == 'RELEASE') {
                name = '已上架';
            } else if (e.value == 'PRIVATE') {
                name = '未上架';
            } else if (e.value == 'NORMAL') {
                name = '启用';
            } else if (e.value = 'FORBIDDEN'){
                name = '禁用';
            } else {
                name = '其他';
            }
            return name;
        }

        window.renderAction = function (e) {
            var row = e.record;
            var html = [
                tools.createActionButton("编辑","icon-edit",function () {
                    edit(row.id);
                }),
                tools.createActionButton("删除版本","icon-remove",function () {
                    require(["message"],function (message) {
                        message.confirm("确定删除该版本？",function () {
                            var loading = message.loading("删除中...");
                            request["delete"]("plugin-version/"+row.id,{},function (res) {
                                loading.close();
                                if (res.status == 200){
                                    grid.reload();
                                } else {
                                    message.showTips("删除失败"+res.message);
                                }
                            })
                        })
                    })
                })
            ];
            return html.join("");
        }

        window.renderPermission = function (e) {
            var row = e.record;
            var html = [
                tools.createActionButton("详情","icon-find",function () {
                    var ids = row.permissionIdList;
                    var win = mini.get("permission-win");
                    win.showAtPos();
                    request.get("plugin-permission/ids?ids=" + ids, function (response) {
                        if (response.status === 200) {
                            // mainForm.setData(response.result);
                            // var grid = window.grid = mini.get("datagrid-permission");
                            // tools.initGrid(grid);
                            //
                            // grid.setData(response.result);
                        } else {
                            //
                        }
                    });
                })
            ];
            return html.join("");
        }

    });
});