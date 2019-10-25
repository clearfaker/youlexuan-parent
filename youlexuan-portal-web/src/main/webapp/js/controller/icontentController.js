//广告控制层（运营商后台）
app.controller("ics",function($scope,$controller,contentService){



    $scope.contentList=[];//广告集合
    $scope.findByCategoryId=function(categoryId){


        contentService.findByCategoryId(categoryId).success(
            function(response){
                $scope.contentList[categoryId]=response;
            }
        );
    }
});