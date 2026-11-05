# Chạy test trên Windows (PowerShell)
#   .\run-tests.ps1                 -> smoke, có mở trình duyệt
#   .\run-tests.ps1 regression      -> toàn bộ, headless
#   .\run-tests.ps1 regression -Report  -> chạy xong mở Allure report
param(
    [ValidateSet("smoke", "regression")] [string]$Suite = "smoke",
    [switch]$Report
)

$headless = if ($Suite -eq "regression") { "true" } else { "false" }
mvn clean test "-DsuiteXml=testng-$Suite.xml" "-Dheadless=$headless"

if ($Report) {
    mvn allure:serve
}
