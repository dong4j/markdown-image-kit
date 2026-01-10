package info.dong4j.idea.plugin.chain;

import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;

import info.dong4j.idea.plugin.MikBundle;
import info.dong4j.idea.plugin.console.MikConsoleView;

import java.util.ArrayList;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

/**
 * 进度跟踪器
 * <p>
 * 统一管理责任链处理的进度展示，提供基于权重的进度计算和分层的进度信息展示。
 * 支持步骤级和项目级的进度跟踪，确保用户能够清晰地看到整体进度和当前处理的详细信息。
 *
 * @author dong4j
 * @version 2.1.0
 * @date 2025.01.XX
 * @since 2.1.0
 */
@Slf4j
public class ProgressTracker {
    /**
     * 步骤信息
     */
    private static class StepInfo {
        /** 步骤名称 */
        String name;
        /** 步骤权重，用于计算在总进度中的占比 */
        int weight;
        /** 步骤索引 */
        int index;

        StepInfo(String name, int weight, int index) {
            this.name = name;
            this.weight = weight;
            this.index = index;
        }
    }

    /** 进度指示器 */
    private final ProgressIndicator indicator;
    /** 主任务标题 */
    private final String mainTaskTitle;
    /** 所有步骤信息 */
    private final List<StepInfo> steps;
    /** 总权重，用于计算进度 */
    private final int totalWeight;
    /** 当前步骤索引 */
    private int currentStepIndex = -1;
    /** 已完成步骤的累计权重 */
    private int completedWeight = 0;
    /** 是否启用控制台输出 */
    private final boolean enableConsoleOutput;
    /** 任务开始时间 */
    private final long startTime;
    /** 项目实例 */
    private final Project project;

    /**
     * 创建进度跟踪器
     *
     * @param indicator     进度指示器
     * @param project       项目实例
     * @param mainTaskTitle 主任务标题
     * @param steps         步骤名称列表
     */
    public ProgressTracker(ProgressIndicator indicator, Project project, String mainTaskTitle, List<String> steps) {
        this(indicator, project, mainTaskTitle, steps, null, true);
    }

    /**
     * 创建进度跟踪器（支持自定义权重）
     *
     * @param indicator     进度指示器
     * @param project       项目实例
     * @param mainTaskTitle 主任务标题
     * @param steps         步骤名称列表
     * @param weights       步骤权重列表（如果为 null，则使用默认权重）
     */
    public ProgressTracker(ProgressIndicator indicator, Project project, String mainTaskTitle, List<String> steps, List<Integer> weights) {
        this(indicator, project, mainTaskTitle, steps, weights, true);
    }

    /**
     * 创建进度跟踪器（支持自定义权重和控制台输出）
     *
     * @param indicator           进度指示器
     * @param project             项目实例
     * @param mainTaskTitle       主任务标题
     * @param steps               步骤名称列表
     * @param weights             步骤权重列表（如果为 null，则使用默认权重）
     * @param enableConsoleOutput 是否启用控制台输出
     */
    public ProgressTracker(ProgressIndicator indicator, Project project, String mainTaskTitle, List<String> steps, List<Integer> weights,
                           boolean enableConsoleOutput) {
        this.indicator = indicator;
        this.project = project;
        this.mainTaskTitle = mainTaskTitle;
        this.steps = new ArrayList<>();
        this.enableConsoleOutput = enableConsoleOutput;
        this.startTime = System.currentTimeMillis();

        int total = 0;
        for (int i = 0; i < steps.size(); i++) {
            int weight = (weights != null && i < weights.size()) ? weights.get(i) : getDefaultWeight(steps.get(i));
            this.steps.add(new StepInfo(steps.get(i), weight, i));
            total += weight;
        }
        this.totalWeight = total;

        // 输出任务开始信息到控制台
        if (enableConsoleOutput) {
            MikConsoleView.printSmart(project, "========================================");
            MikConsoleView.printSmart(project, "【任务开始】" + mainTaskTitle);
            MikConsoleView.printSmart(project, "【子流程】共 " + steps.size() + " 个步骤: " + String.join(" → ", steps));
            MikConsoleView.printSmart(project, "========================================");
        }
    }

    /**
     * 根据步骤名称获取默认权重
     * <p>
     * 轻量级步骤（权重 1）：解析文件、重命名、标签转换、写入文档等
     * 中量级步骤（权重 3）：压缩图片、下载图片等
     * 重量级步骤（权重 10）：上传图片等耗时操作
     *
     * @param stepName 步骤名称
     * @return 权重值
     */
    private int getDefaultWeight(String stepName) {
        // 根据步骤名称判断权重
        if (stepName != null) {
            String lowerName = stepName.toLowerCase();
            // 上传操作权重最高
            if (lowerName.contains("upload") || lowerName.contains("上传")) {
                return 10;
            }
            // 下载和压缩操作权重中等
            if (lowerName.contains("download") || lowerName.contains("下载") ||
                lowerName.contains("compress") || lowerName.contains("压缩")) {
                return 3;
            }
        }
        // 其他操作权重最低
        return 1;
    }

    /**
     * 开始一个新步骤
     *
     * @param stepIndex 步骤索引
     */
    public void startStep(int stepIndex) {
        if (stepIndex < 0 || stepIndex >= steps.size()) {
            log.debug("无效的步骤索引: {}", stepIndex);
            return;
        }

        // 如果切换步骤，标记上一个步骤完成
        if (currentStepIndex >= 0 && currentStepIndex != stepIndex) {
            completedWeight += steps.get(currentStepIndex).weight;

            // 输出上一步骤完成信息
            if (enableConsoleOutput) {
                StepInfo prevStep = steps.get(currentStepIndex);
                MikConsoleView.printSmart(project, "[✓] 步骤 " + (currentStepIndex + 1) + "/" + steps.size() + " 完成: " + prevStep.name);
            }
        }

        currentStepIndex = stepIndex;
        StepInfo step = steps.get(stepIndex);

        // 输出步骤开始信息到控制台
        if (enableConsoleOutput) {
            MikConsoleView.printSmart(project, "");
            MikConsoleView.printSmart(project, "[▶] 步骤 " + (stepIndex + 1) + "/" + steps.size() + " 开始: " + step.name);
        }

        // 检查 indicator 是否可用（预览模式下可能为 null）
        if (indicator == null) {
            return;
        }

        try {
            // 更新主文本：保持主任务标题不变
            indicator.setText(mainTaskTitle);

            // 更新副文本：显示当前子流程和进度
            String subText = String.format("%s (%d/%d)", step.name, stepIndex + 1, steps.size());
            indicator.setText2(subText);

            // 更新进度（步骤开始时的进度）
            updateProgress(0.0);
        } catch (Exception e) {
            // 在预览模式下可能会抛出 SideEffectNotAllowedException，忽略这些异常
            log.debug("更新进度时发生异常（可能是预览模式）: {}", e.getMessage());
        }
    }

    /**
     * 更新步骤内的项目进度
     *
     * @param stepIndex 步骤索引
     * @param itemName  当前处理的项目名称（如图片文件名）
     * @param current   当前已处理的项目数
     * @param total     总项目数
     */
    @SuppressWarnings("D")
    public void updateItemProgress(int stepIndex, String itemName, int current, int total) {
        if (stepIndex < 0 || stepIndex >= steps.size()) {
            log.debug("无效的步骤索引: {}", stepIndex);
            return;
        }

        // 输出进度到控制台（每 10% 或关键节点输出一次，避免刷屏）
        if (enableConsoleOutput && total > 0) {
            int percentage = (current * 100) / total;
            int prevPercentage = ((current - 1) * 100) / total;

            // 在每 10% 的进度点输出，或者是第一个和最后一个
            if (current == 1 || current == total || (percentage / 10 > prevPercentage / 10)) {
                String progressInfo = String.format("  [%3d%%] 处理: %s (%d/%d)", percentage, itemName != null ? itemName : "项目", current,
                                                    total);
                MikConsoleView.printSmart(project, progressInfo);
            }
        }

        // 检查 indicator 是否可用（预览模式下可能为 null）
        if (indicator == null) {
            return;
        }

        StepInfo step = steps.get(stepIndex);

        // 计算步骤内的进度比例
        double stepProgress = total > 0 ? (current * 1.0) / total : 0.0;

        try {
            // 更新主文本：保持主任务标题不变
            indicator.setText(mainTaskTitle);

            // 更新副文本：组合显示子流程信息和文件处理进度
            StringBuilder subText = new StringBuilder();

            // 子流程信息：步骤名称 (当前步骤/总步骤)
            subText.append(String.format("%s (%d/%d)", step.name, stepIndex + 1, steps.size()));

            // 添加详细的文件处理进度
            if (itemName != null && !itemName.isEmpty()) {
                subText.append(" - ");
                subText.append(MikBundle.message("mik.action.processing.title", itemName));
                if (total > 1) {
                    subText.append(String.format(" (%d/%d)", current, total));
                }
            } else if (total > 1) {
                subText.append(String.format(" - (%d/%d)", current, total));
            }

            indicator.setText2(subText.toString());

            // 计算总体进度
            // 已完成步骤的进度 + 当前步骤的进度
            double overallProgress = (completedWeight + step.weight * stepProgress) / totalWeight;
            updateProgress(overallProgress);
        } catch (Exception e) {
            // 在预览模式下可能会抛出 SideEffectNotAllowedException，忽略这些异常
            log.debug("更新进度时发生异常（可能是预览模式）: {}", e.getMessage());
        }
    }

    /**
     * 更新进度（内部方法）
     *
     * @param progress 进度值（0.0 - 1.0）
     */
    private void updateProgress(double progress) {
        // 检查 indicator 是否可用（预览模式下可能为 null）
        if (indicator == null) {
            return;
        }

        try {
            // 确保进度在有效范围内
            progress = Math.max(0.0, Math.min(1.0, progress));
            indicator.setFraction(progress);
        } catch (Exception e) {
            // 在预览模式下可能会抛出 SideEffectNotAllowedException，忽略这些异常
            log.debug("设置进度时发生异常（可能是预览模式）: {}", e.getMessage());
        }
    }

    /**
     * 完成所有步骤
     */
    public void finish() {
        currentStepIndex = steps.size() - 1;
        completedWeight = totalWeight;

        // 输出任务完成信息到控制台
        if (enableConsoleOutput) {
            long duration = System.currentTimeMillis() - startTime;
            MikConsoleView.printSmart(project, "");
            MikConsoleView.printSmart(project, "========================================");
            MikConsoleView.printSmart(project, "【任务完成】" + mainTaskTitle);
            MikConsoleView.printSmart(project, "【耗时】" + formatDuration(duration));
            MikConsoleView.printSmart(project, "【状态】所有步骤执行完成");
            MikConsoleView.printSmart(project, "========================================\n\n");
        }

        // 检查 indicator 是否可用（预览模式下可能为 null）
        if (indicator == null) {
            return;
        }

        try {
            // 更新主文本：保持主任务标题不变
            indicator.setText(mainTaskTitle);

            // 更新副文本：显示最后一个子流程完成
            if (!steps.isEmpty()) {
                StepInfo lastStep = steps.get(steps.size() - 1);
                indicator.setText2(String.format("%s (%d/%d)", lastStep.name, steps.size(), steps.size()));
            } else {
                indicator.setText2("");
            }
            
            updateProgress(1.0);
        } catch (Exception e) {
            // 在预览模式下可能会抛出 SideEffectNotAllowedException，忽略这些异常
            log.debug("完成步骤时发生异常（可能是预览模式）: {}", e.getMessage());
        }
    }

    /**
     * 获取总步骤数
     *
     * @return 总步骤数
     */
    public int getTotalSteps() {
        return steps.size();
    }

    /**
     * 格式化时长
     *
     * @param millis 毫秒数
     * @return 格式化后的时长字符串
     */
    private String formatDuration(long millis) {
        if (millis < 1000) {
            return millis + "ms";
        } else if (millis < 60000) {
            return String.format("%.2fs", millis / 1000.0);
        } else {
            long seconds = millis / 1000;
            long minutes = seconds / 60;
            seconds = seconds % 60;
            return String.format("%dm %ds", minutes, seconds);
        }
    }
}

