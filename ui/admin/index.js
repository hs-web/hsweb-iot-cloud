importResource("/admin/css/common.css");
// importResource("/plugins/font-awesome/4.7.0/css/font-awesome.css");
importResource("/admin/index.css");
require([
    // "css!index/third-party/scrollbar/jquery.mCustomScrollbar",
    "css!index/frame1/res/menu/menu",
    "css!index/frame1/res/tabs",
    "css!index/frame1/res/frame",
    "css!index/frame1/res/index",
    "css!plugin/miniui/themes/default/flatstyle"
]);
require([
    "index/frame1/res/menu/menu",
    "index/frame1/res/menutip"
], function () {

//引入miniui,miniui不支持require,使用此方式引入
    importMiniui(function () {

        // importResource("index/third-party/scrollbar/jquery.mCustomScrollbar.concat.min");
        mini.parse();

        var tabs = mini.get("mainTabs");

        function createTemplateTab(tab, id) {
            var el = tabs.getTabBodyEl(tab);
            require(["pages/template/parser"], function (parser) {
                parser($(el), id);
            })
        }

        $(".go-index").on("click", function () {
            tabs.activeTab(tabs.getTab("index"))
        });
        $(".remove-tab").on("click", function () {
            var tab = tabs.getActiveTab();
            if (tab.name !== 'index') {
                tabs.removeTab(tabs.getActiveTab())
            }
        });
        $(".reload-tab").on("click", function () {
            var tab = tabs.getActiveTab();
            if (tab.name !== 'index') {
                if (tab.reload) {
                    tab.reload()
                } else {
                    tabs.reloadTab(tab)
                }
            } else {
                window.dashboardInited = false;
                initDashBoard();
            }
        });
        $(".full-tab").on("click", function () {
            var element = document.body;
            var requestMethod = element.requestFullScreen
                || element.webkitRequestFullScreen //谷歌
                || element.mozRequestFullScreen  //火狐
                || element.msRequestFullScreen; //IE11
            if (requestMethod) {
                requestMethod.call(element);
            }
        });

        function createHLTab(tab, page) {
            var el = tabs.getTabBodyEl(tab);
            require(["pages/module/parser", "text!" + page], function (parser, pageConfig) {
                new parser().parse($(el), JSON.parse(pageConfig));
            });
        }

        window.showTab = function (node) {
            if (!node.url || node.url === "") return;
            var id = "tab$" + node.id;
            var tab = tabs.getTab(id);
            var url = node.url;
            if (!tab) {
                tab = {};
                tab.name = id;
                tab.title = node.name;
                tab.iconCls = node.icon;
                tab.showCloseButton = true;
                tab = tabs.addTab(tab);
                if (url.indexOf("template:") === 0) {
                    createTemplateTab(tab, url.split(":")[1]);
                } else if (url.indexOf(".hl") !== -1) {
                    createHLTab(tab, url);
                } else {
                    if (url.indexOf("http") === -1) {
                        url = BASE_PATH + url;
                    }
                    tab.url = url;
                }
            }
            tabs.activeTab(tab);
        };

        function activeTab(item) {
            showTab(item);
        }

        //menu
        var menu = new Menu("#mainMenu", {
            itemclick: function (item) {
                if (item.url) {
                    activeTab(item);
                }
            }
        });

        window.initMenu = function () {
            require(["request", "message"], function (request, message) {
                // var loading = message.loading("加载用户菜单...")
                var api = "menu/user-own/tree";
                request.get(api, function (response) {
                    // loading.hide();
                    if (response.status === 200) {
                        menu.loadData(response.result);
                    } else {
                        message.showTips("加载菜单失败:" + response.message, "danger");
                    }
                })
            });
        };
        initMenu();
        initLogin();
        $(function () {
            // $(".sidebar").mCustomScrollbar({autoHideScrollbar: true});
        });

        new MenuTip(menu);

        //toggle
        $("#toggle, .sidebar-toggle").click(function () {
            $('body').toggleClass('compact');
            mini.layout();
        });

        //dropdown
        $(".dropdown-toggle").click(function (event) {
            $(this).parent().addClass("open");
            return false;
        });

        $(document).click(function (event) {
            $(".dropdown").removeClass("open");
        });

        function updateLockPwd(call) {
            mini.get('updateLockPwdWindow').show();
            window.onLockPwdUpdate = function () {
                mini.get('updateLockPwdWindow').hide();
                if (call) {
                    call();
                }
            };
        }

        $(".upload-lock-pwd").on("click", function () {
            updateLockPwd(function () {
            });
        });

        $('.do-update-lock-pwd-button')
            .on("click", function () {
                var data = new mini.Form('#updateLockPwdForm').getData();
                require(["request", "message"], function (request, message) {
                    var loading = message.loading("提交中...");
                    request.put("user-lock/lock-password", data, function (response) {
                        loading.hide();
                        if (response.status === 200) {
                            message.showTips("修改成功");
                            if (window.onLockPwdUpdate) {
                                window.onLockPwdUpdate();
                            }
                        } else {
                            mini.alert(response.message);
                        }
                    })
                });
            });
        $(".unlock-button")
            .on('click', function () {
                var form = new mini.Form("#unLockForm");
                require(["request", "message"], function (request, message) {
                    var loading = message.loading("解锁中...");
                    request.put("user-lock/un-lock", form.getData(), function (response) {
                        loading.hide();
                        if (response.status === 200) {
                            mini.get('unLockWindow').hide();
                            if (window.onLock) {
                                window.onLock();
                            }
                        } else {
                            message.showTips(response.message, "danger");
                            mini.getbyName("lockPassword").focus();
                        }
                        form.reset();
                    })
                });
            });
        $(".lock-system")
            .on('click', function () {
                require(["request"], function (request) {
                    request.put("user-lock/lock", {}, function (response) {
                        if (response.status === 200) {
                            if (!response.result) {
                                //提示修改锁屏密码
                                updateLockPwd(function () {
                                    $(".lock-system").click();
                                });
                            } else {
                                mini.get('unLockWindow').show();
                            }
                        }
                    })
                });

            })
    });
})

function initDashBoard() {
    if (window.dashboardInited) {
        return;
    }
    require(['./dashboard/dashboard', 'request'], function (DashBoard, request) {
        request.get("dashboard-user", function (response) {
            if (response.status === 200) {
                window.dashboardInited = true;
                new DashBoard({
                    target: $(".dashboard").html(''),
                    config: response.result
                }).init();
                mini.parse();
            }
        });

    });
}

function initLogin() {
    function doLogin() {
        require(["request", "message"], function (request, message) {
            var form = new mini.Form("#loginWindow");
            form.validate();
            if (form.isValid() === false) return;
            var loding = message.loading("登录中...");
            var data = form.getData();
            request.post("authorize/login", {
                token_type: "iot-cloud-user",
                username: data.username,
                password: data.password
            }, function (e) {
                loding.hide();
                if (e.status === 200) {
                    mini.get("loginWindow").hide();
                    require(["storejs"], function (store) {
                        store.set("iot-cloud-user", e.result.token);
                        if (window.onLoginSuccess) {
                            window.onLoginSuccess();
                        }
                    });

                } else {
                    mini.alert(e.message);
                }
            });
        });
    }

    mini.getbyName("password").on("enter", doLogin);
    //绑定登录事件
    $(".login-button").on("click", doLogin);

    $(".reset-button").on("click", function () {
        new mini.Form("#loginWindow").reset();
    });
    $(".login-out").on("click", function () {
        require(["request", "message"], function (request, message) {
            message.confirm("确认退出系统?", function () {
                request.put("user-lock/logout", {}, function (reponse) {
                    window.location.reload();
                });
            });
        });
    });
    initAuthorize();
    // initMenu();
    $(".settings").on("click", function () {
        require(["miniui-tools"], function (tools) {
            tools.openWindow("admin/me/info.html", "个人信息", "800", "600", function () {

            });
        });
    });
}

window.doLogin = function (callback, msg) {
    //登录超时
    if (msg.result === 'expired'||msg.result.value==='expired') {
        mini.get("loginWindow").show();
        window.onLoginSuccess = function () {
            initAuthorize(callback);
        };
    }
    //锁定
    if (msg.result === 'lock'||msg.result.value==='lock') {
        //显示解锁界面
        mini.get('unLockWindow').show();
        window.onLock = function () {
            callback();
            initDashBoard();
            initMenu();
        }
    }
    //被踢下线了
    if (msg.result === 'offline'||msg.result.value==='offline') {
        if (!window.offlineTips) {
            mini.alert("该用户已经在其他地方登录!", "", function () {
                require(["request"], function (request) {
                    request.put("user-lock/logout", {}, function () {
                        window.location.reload();
                    });
                });
            });
            window.offlineTips = true;
        }
    }
    //被禁止访问了
    if (msg.result === 'deny'||msg.result.value==='deny') {
    }
};

function initAuthorize(call) {
    require(["authorize", 'request'], function (authorize, request) {
        authorize.init(function () {
            initDashBoard();
            $(".username").text(authorize.user.name);
            if (call) {
                call();
            }
            request.get("person/me/authorization", function (me) {
                me = me.result;
                if (me && me.personnel) {
                    $(".username").text(me.personnel.name);
                    $(".user-img").attr("src", me.personnel.photo);
                }
            });
            initMenu();
        });
    });
}
