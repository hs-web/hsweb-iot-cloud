importResource("/admin/css/common.css");

importMiniui(function () {
    mini.parse();
    require(["request", "miniui-tools", "search-box"], function (request, tools, SearchBox) {

        new SearchBox({
            container: $("#search-box"),
            onSearch: search,
            initSize: 2
        }).init();

        var grid = window.grid = mini.get("product-grid");
        tools.initGrid(grid);

        grid.setUrl(request.basePath + "plugin-product");

        function search() {
            tools.searchGrid("#search-box", grid);
        }
        search();

        function edit(id){
            tools.openWindow("admin/plugin/product/save.html?id="+id,"编辑产品","600","800",function () {
                grid.reload();
            })
        }

        $(".add-button").click(function () {
            tools.openWindow("admin/plugin/product/save.html","新建产品","600","800",function () {
                grid.reload();
            })
        });

        window.renderAction = function (e) {
            var row = e.record;
            var html = [
                tools.createActionButton("编辑","icon-edit",function () {
                    edit(row.id);
                }),
                tools.createActionButton("删除","icon-remove",function () {
                    require(["message"],function (message) {
                        message.confirm("确定删除该产品？",function () {
                            var loading = message.loading("删除中...");
                            request["delete"]("plugin-product/"+row.id,{},function (res) {
                                loading.close();
                                if (res.status === 200){
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
})