@echo off

rem Copyright (C) 2008-2012 SonarSource
rem mailto:contact AT sonarsource DOT com
rem 
rem Sonar is free software; you can redistribute it and/or
rem modify it under the terms of the GNU Lesser General Public
rem License as published by the Free Software Foundation; either
rem version 3 of the License, or (at your option) any later version.
rem 
rem Sonar is distributed in the hope that it will be useful,
rem but WITHOUT ANY WARRANTY; without even the implied warranty of
rem MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
rem Lesser General Public License for more details.
rem
rem You should have received a copy of the GNU Lesser General Public
rem License along with Sonar; if not, write to the Free Software
rem Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
rem
rem Use this DOS script to create a Sonar WAR archive.

set ORIG_ANT_HOME=%ANT_HOME%
set ANT_HOME=%CD%\apache-ant-1.7.0

call apache-ant-1.7.0\bin\ant

set ANT_HOME=%ORIG_ANT_HOME%
set ORIG_ANT_HOME=
