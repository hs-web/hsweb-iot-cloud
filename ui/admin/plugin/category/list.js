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

        grid.setUrl(request.basePath+"plugin-category");
        function search() {
            tools.searchGrid("#search-box", grid);
        }

        search();


        $(".search-button").click(search);
        tools.bindOnEnter("#search-box",search);

        $(".add-button").click(function () {
            tools.openWindow("admin/plugin/category/save.html","添加种类","800","400",function () {
                grid.reload();
            })
        });

        function edit(id){
            tools.openWindow("admin/plugin/category/save.html?id="+id,"编辑种类","800","600",function () {
                grid.reload();
            })
        }

        window.renderAction = function (e) {
            var row = e.record;
            var html = [
                tools.createActionButton("编辑","icon-edit",function () {
                    edit(row.id);
                }),
                tools.createActionButton("删除","icon-remove",function () {
                    require(["message"],function (message) {
                        message.confirm("确定删除该种类？",function () {
                            var loading = message.loading("删除中...");
                            request["delete"]("plugin-category/"+row.id,{},function (res) {
                                loading.close();
                                if (res.status === 200){
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