# gdream.github.io

# window 运行该博客
## 安装
> https://idratherbewriting.com/documentation-theme-jekyll/mydoc_install_jekyll_on_windows.html#install-bundler

Install Ruby and Ruby Development Kit
First you must install Ruby because Jekyll is a Ruby-based program and needs Ruby to run.

Go to RubyInstaller for Windows.
Under RubyInstallers, download and install one of the Ruby installers under the WITH DEVKIT list (usually the recommended/highlighted option).
Double-click the downloaded file and proceed through the wizard to install it. Run the ridk install step on the last stage of the installation wizard.
Open a new command prompt window or Git Bash session.
Install the Jekyll gem
At this point you should have Ruby and Rubygem on your machine.

Now use gem to install Jekyll:
```bash
gem install jekyll
gem install bundler
bundle install 
bundle add webrick
bundle exec jekyll serve  # alternatively, npm start
```