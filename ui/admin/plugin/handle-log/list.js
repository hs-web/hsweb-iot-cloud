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

        grid.setUrl(request.basePath+"plugin-version");
        function search() {
            tools.searchGrid("#search-box", grid);
        }

        search();


        window.renderAction = function (e) {
            var row = e.record;
            var html = [
                tools.createActionButton("编辑","icon-edit",function () {
                    edit(row.id);
                }),
                tools.createActionButton("版本详情","icon-find",function () {
                   console.log("插件版本");
                }),
                tools.createActionButton("删除版本","icon-remove",function () {
                    require(["message"],function (message) {
                        message.confirm("确定删除该版本？",function () {
                            var loading = message.loading("删除中...");
                            request["delete"]("plugin-version/"+row.id,{},function (res) {
                                loading.close();
                                if (res.status == 200){
                                    // console.log(res.status);
                                    // e.sender.removeNode(row);
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

    });
});