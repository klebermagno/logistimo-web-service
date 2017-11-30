angular.module('logistimo.storyboard.temperatureStatusDonutWidget', [])
    .config(function (widgetsRepositoryProvider) {
        widgetsRepositoryProvider.addWidget({
            id: "temperatureStatusDonutWidget",
            name: "Asset status donut",
            templateUrl: "plugins/storyboards/assets/temperature-status-donut-widget/temperature-status-donut-widget.html",
            editTemplateUrl: "plugins/storyboards/assets/asset-edit-template.html",
            templateFilters: [
                {
                    nameKey: 'asset.status',
                    type: 'assetStatus'
                },
                {
                    nameKey: 'asset.type',
                    type: 'assetType'
                },
                {
                    nameKey: 'period.since',
                    type: 'tPeriod'
                },
                {
                    nameKey: 'exclude.temp.state',
                    type: 'exTempState'
                },
                {
                    nameKey: 'include.entity.tag',
                    type: 'entityTag'
                },
                {
                    nameKey: 'exclude.entity.tag',
                    type: 'exEntityTag'
                }
            ],
            defaultHeight: 3,
            defaultWidth: 4
        });
    })
    .controller('temperatureStatusDonutWidgetController',
    ['$scope', 'dashboardService', 'domainCfgService', 'INVENTORY', function ($scope, dashboardService,
                                                                              domainCfgService, INVENTORY) {
        var filter = angular.copy($scope.widget.conf);
        var tempPieColors, tempPieOrder, asset = '';
        var fDate = (checkNotNullEmpty(filter.date) ? formatDate(filter.date) : undefined);
        $scope.showChart = false;
        $scope.wloading = true;
        $scope.showError = false;
        function setFilters() {

            if (checkNotNullEmpty(filter.assetStatus)) {
                var assetStatus = $scope.assetStatus = filter.assetStatus;
                if (assetStatus == 'tn') {
                    tempPieColors[1] = tempPieColors[3];
                    tempPieColors[2] = tempPieColors[3];
                    $scope.widget.conf.title = "Normal";
                } else if (assetStatus == 'tl') {
                    tempPieColors[0] = tempPieColors[3];
                    tempPieColors[2] = tempPieColors[3];
                    $scope.widget.conf.title = "Freezing";
                } else if (assetStatus == 'th') {
                    tempPieColors[0] = tempPieColors[3];
                    tempPieColors[1] = tempPieColors[3];
                    $scope.widget.conf.title = "Heating";
                } else {
                    tempPieColors[0] = tempPieColors[3];
                    tempPieColors[1] = tempPieColors[3];
                    tempPieColors[2] = tempPieColors[3];
                    $scope.widget.conf.title = "Unknown";
                }
            }

        }


        if (checkNotNullEmpty($scope.widget.conf.asset) && $scope.widget.conf.asset.length > 0) {
            var first = true;
            $scope.widget.conf.asset.forEach(function (data) {
                if (!first) {
                    asset += "," + data.id;
                } else {
                    asset += data.id;
                    first = false;
                }

            });
        }


        domainCfgService.getSystemDashboardConfig().then(function (data) {
            var domainConfig = angular.fromJson(data.data);
            tempPieColors = domainConfig.pie.tc;
            tempPieOrder = domainConfig.pie.to;
        }).then(function () {
            setFilters();
        }).then(function () {
            getData();
        });

        function getData() {
            var chartData = [], totalAssets = 0, totalAssetsText = '';
            dashboardService.get(undefined, undefined, $scope.exFilter, $scope.exType, $scope.period,
                $scope.widget.conf.tPeriod, asset, constructModel(filter.entityTag), fDate,
                constructModel(filter.exEntityTag), false).then(function (data) {
                    chartData = constructPieData(data.data.tempDomain, tempPieColors, tempPieOrder, INVENTORY,
                        $scope.mapEvent, $scope.widget.conf.exTempState);
                    var normalPercent = getPercent(data.data.tempDomain, $scope.assetStatus);
                    totalAssets = getItemCount(data.data.tempDomain, $scope.assetStatus);

                if(totalAssets>1){
                    totalAssetsText = totalAssets + " assets";
                }else{
                    totalAssetsText = totalAssets + " asset";
                }
                    setWidgetData(normalPercent, chartData, totalAssetsText);
                }).catch(function error(msg) {
                    showError(msg, $scope);
                }).finally(function () {
                    $scope.loading = false;
                    $scope.wloading = false;
                });
        }

        function setWidgetData(centerLabelPercent, chartData, totalAssetsText) {
            var radius = getDonutRadius($scope.widget.width,$scope.widget.height);
            $scope.temperatureStatusDonutWidget = {
                wId: $scope.widget.id,
                cType: "doughnut2d",
                copt: {
                    theme: "fint",
                    doughnutRadius: radius.doughnutRadius,
                    pieRadius: radius.pieRadius,
                    showLabels: "0",
                    showPercentValues: "0",
                    showLegend: "0",
                    showValues: "0",
                    defaultCenterLabel: centerLabelPercent,
                    subCaption: totalAssetsText,
                    captionOnTop: "0",
                    alignCaptionWithCanvas: "1",
                    showToolTip: "0",
                    centerLabelFontSize: 19,
                    centerLabelFont : 'Helvetica Neue, Arial'
                },
                cdata: chartData,
                computedWidth: '100%',
                computedHeight: parseInt($scope.widget.computedHeight, 10) - 30
            };
            $scope.wloading = false;
            $scope.showChart = true;
        }
    }]);

logistimoApp.requires.push('logistimo.storyboard.temperatureStatusDonutWidget');