# 构建产物输出目录
IDEA_PLUGINS_DIR := /Users/dong4j/Developer/4.Tools/JetBrains/IDEA/plugins
MIK_DIR := .

# asdf Java 环境支持
SHELL := /bin/zsh

build-plugin:
	@echo "正在构建 mik 插件..."
	@ASDF_CMD='$(ASDF_INIT)'; \
		if ! eval "$$ASDF_CMD"; then \
			echo "提示: 未找到 $(ASDF_SH), 将尝试使用系统 Java"; \
		fi; \
		if ! java -version >/dev/null 2>&1; then \
			echo "错误: 未找到 Java 运行环境, 请通过 asdf 安装并激活 Java (例如: asdf install java temurin-17 && asdf global java temurin-17)"; \
			exit 1; \
		fi; \
		cd $(MIK_DIR) && eval "$$ASDF_CMD" && ./gradlew buildPlugin

# 拷贝构建产物到 IDEA 插件目录（解压后拷贝目录）
install-plugins: build-plugin
	@TARGET=$(IDEA_PLUGINS_DIR); \
	echo "正在安装插件到 $$TARGET..."; \
	mkdir -p $$TARGET; \
	for dir in $(MIK_DIR); do \
		zip_file=$$(ls -t $$dir/build/distributions/*.zip 2>/dev/null | head -n1); \
		if [ -n "$$zip_file" ]; then \
			temp_dir=$$(mktemp -d); \
			echo "  解压 $$zip_file..."; \
			unzip -q -o $$zip_file -d $$temp_dir; \
			plugin_dir=$$(find $$temp_dir -maxdepth 1 -type d ! -path $$temp_dir | head -n1); \
			if [ -n "$$plugin_dir" ] && [ -d $$plugin_dir ]; then \
				plugin_name=$$(basename $$plugin_dir); \
				target_plugin_dir=$$TARGET/$$plugin_name; \
				echo "  拷贝 $$plugin_dir -> $$target_plugin_dir"; \
				rm -rf $$target_plugin_dir; \
				mv $$plugin_dir $$target_plugin_dir; \
			else \
				echo "  警告: 解压后未找到插件目录"; \
			fi; \
			rm -rf $$temp_dir; \
		else \
			echo "  警告: 未找到 $$dir 的构建产物"; \
		fi; \
	done; \
	echo "✓ 插件安装完成,请重启 IDEA 以应用更改"
