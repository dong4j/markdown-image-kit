/*
 * MIT License
 *
 * Copyright (c) 2021 dong4j <dong4j@gmail.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package info.dong4j.idea.plugin.action.markdown;

import info.dong4j.idea.plugin.client.CustomOssClient;
import info.dong4j.idea.plugin.client.OssClient;
import info.dong4j.idea.plugin.enums.CloudEnum;
import info.dong4j.idea.plugin.icon.MikIcons;
import info.dong4j.idea.plugin.settings.MikPersistenComponent;
import info.dong4j.idea.plugin.settings.OssState;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;

/**
 * <p>Company: no company</p>
 * <p>Description: 上传到自定义 OSS 事件</p>
 *
 * @author dong4j
 * @version 1.0.0
 * @email "mailto:dong4j@gmail.com"
 * @date 2021.02.14 18:40
 * @since 1.5.0
 */
public final class UploadCustomCloudAction extends UploadActionBase {

    /**
     * Gets icon *
     *
     * @return the icon
     * @since 1.5.0
     */
    @Contract(pure = true)
    @Override
    protected Icon getIcon() {
        return MikIcons.CUSTOM;
    }

    /**
     * Is available
     *
     * @return the boolean
     * @since 1.5.0
     */
    @Contract(pure = true)
    @Override
    boolean isAvailable() {
        return OssState.getStatus(MikPersistenComponent.getInstance().getState().getCustomOssState());
    }

    /**
     * Gets name *
     *
     * @return the name
     * @since 1.5.0
     */
    @Nullable
    @Contract(pure = true)
    @Override
    String getName() {
        return CloudEnum.CUSTOMIZE.title;
    }

    /**
     * Gets client *
     *
     * @return the client
     * @since 1.5.0
     */
    @Contract(pure = true)
    @Override
    OssClient getClient() {
        return CustomOssClient.getInstance();
    }
}
