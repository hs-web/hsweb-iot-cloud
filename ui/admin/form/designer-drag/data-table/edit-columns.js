(function () {
    function ColumnsConfigEditor(conf) {
        conf = conf || {};
        var grid = mini.get("column-config-datagrid");
        var windowId = "edit-column-window";
        var container;
        var win;
        if (conf.container) {
            container = $(conf.container);
        } else {
            container = $("#edit-column-container");
            if (container.length === 0) {
                var win = $("<div id='edit-column-window' allowResize='true' class='mini-window' showToolbar='true' allowResize='false' title='表格列配置' style='width: 800px;height: 600px'>")
                    .append($("<div property='toolbar'>"))
                    .append($("<div class='mini-fit' id='edit-column-container'>"));
                $(document.body).append(win);
                mini.parse();
            }
            win = mini.get(windowId);
            container = $("#edit-column-container");
        }
        conf.onload = conf.onload || function () {

        }
        this.setData = function (data) {
            grid.setData(data);
        }
        this.show = function () {
            if (win) {
                win.show();
            }
        }

        function initGrid() {
            grid.getColumn('renderer').renderer = function () {
                return "";
            }
        }

        var me = this;

        if (!grid) {
            require(['text!pages/form/designer-drag/data-table/edit-columns.html'], function (html) {
                container.append(html);
                mini.parse();
                grid = mini.get("column-config-datagrid");
                initGrid();
                conf.onload.call(me);
            });
        } else {
            conf.onload.call(me);
        }

    }

    if (window.define) {
        window.define([], function () {
            return ColumnsConfigEditor;
        });
    } else {
        window.ColumnsConfigEditor = ColumnsConfigEditor;
    }

})();