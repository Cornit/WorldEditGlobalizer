@echo off

set help="Use: ./build.bat <sponge|bukkit>"


if "%1" == "" (
echo %help%
) else if "%1" == "sponge" (
echo ----Building Bungee----
call gradle :WorldEditGlobalizerBungee:build
echo ----Building Sponge----
call gradle :WorldEditGlobalizerSponge:build
echo ----Building Finished----
)