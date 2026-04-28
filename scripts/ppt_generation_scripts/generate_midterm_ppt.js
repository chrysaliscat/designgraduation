const fs = require("fs");
const path = require("path");

const PptxGenJS = require("pptxgenjs");

const root = "G:\\SYSTEM_DESIGN\\RuoYi-Vue";
const outputPpt = path.join(root, "农产品销售平台_中期检查汇报.pptx");
const outputNotes = path.join(root, "农产品销售平台_中期检查讲稿.txt");

const pptx = new PptxGenJS();
pptx.layout = "LAYOUT_WIDE";
pptx.author = "Codex";
pptx.company = "OpenAI";
pptx.subject = "中期检查汇报";
pptx.title = "基于 SpringBoot + Vue 的农产品销售平台";
pptx.lang = "zh-CN";
pptx.theme = {
  headFontFace: "Microsoft YaHei",
  bodyFontFace: "Microsoft YaHei",
  lang: "zh-CN"
};

const colors = {
  green: "2E7D32",
  greenSoft: "F6FBF6",
  border: "DCE7DC",
  text: "333333",
  sub: "6B6B6B",
  warn: "B57A10",
  warnBg: "FFF7E6"
};

function baseSlide(slide, title, subtitle = "") {
  slide.addShape(pptx.ShapeType.rect, {
    x: 0,
    y: 0,
    w: 0.18,
    h: 7.1,
    fill: { color: colors.green },
    line: { color: colors.green }
  });
  slide.addShape(pptx.ShapeType.rect, {
    x: 0,
    y: 0,
    w: 13.333,
    h: 0.1,
    fill: { color: colors.green },
    line: { color: colors.green }
  });
  slide.addText(title, {
    x: 0.6,
    y: 0.32,
    w: 7.2,
    h: 0.4,
    fontFace: "Microsoft YaHei",
    fontSize: 22,
    bold: true,
    color: "202020"
  });
  if (subtitle) {
    slide.addText(subtitle, {
      x: 0.62,
      y: 0.82,
      w: 7.6,
      h: 0.25,
      fontFace: "Microsoft YaHei",
      fontSize: 9.5,
      color: colors.sub
    });
  }
  slide.addText("中期检查汇报", {
    x: 10.2,
    y: 6.68,
    w: 2.5,
    h: 0.2,
    fontFace: "Microsoft YaHei",
    fontSize: 8.5,
    color: "8A8A8A",
    align: "right"
  });
}

function addCard(slide, x, y, w, h, title, lines, opts = {}) {
  const fill = opts.fill || colors.greenSoft;
  const titleColor = opts.titleColor || colors.green;
  slide.addShape(pptx.ShapeType.roundRect, {
    x, y, w, h,
    rectRadius: 0.08,
    fill: { color: fill },
    line: { color: colors.border, pt: 1 }
  });
  slide.addText(title, {
    x: x + 0.18,
    y: y + 0.12,
    w: w - 0.36,
    h: 0.28,
    fontFace: "Microsoft YaHei",
    fontSize: 12.5,
    bold: true,
    color: titleColor
  });
  slide.addText(
    lines.map((t) => ({ text: t, options: { bullet: { indent: 12 } } })),
    {
      x: x + 0.2,
      y: y + 0.48,
      w: w - 0.35,
      h: h - 0.55,
      fontFace: "Microsoft YaHei",
      fontSize: 10.5,
      color: "4A4A4A",
      breakLine: false,
      paraSpaceAfterPt: 8,
      valign: "top",
      margin: 0.03
    }
  );
}

function addBullets(slide, x, y, w, h, items, fontSize = 16) {
  slide.addText(
    items.map((t) => ({ text: t, options: { bullet: { indent: 16 } } })),
    {
      x, y, w, h,
      fontFace: "Microsoft YaHei",
      fontSize,
      color: colors.text,
      paraSpaceAfterPt: 11,
      breakLine: false,
      margin: 0.03,
      valign: "top"
    }
  );
}

// 1 封面
{
  const slide = pptx.addSlide();
  baseSlide(slide, "基于 SpringBoot + Vue 的农产品销售平台", "毕业设计中期检查汇报");
  slide.addShape(pptx.ShapeType.roundRect, {
    x: 0.75,
    y: 1.45,
    w: 11.1,
    h: 5.0,
    rectRadius: 0.08,
    fill: { color: "F8FBF7" },
    line: { color: colors.border, pt: 1.2 }
  });
  slide.addText("中期汇报内容", {
    x: 1.2,
    y: 2.5,
    w: 3.6,
    h: 0.45,
    fontFace: "Microsoft YaHei",
    fontSize: 24,
    bold: true,
    color: "2A5A2F"
  });
  slide.addText("项目背景、系统设计、当前进展、存在问题、下一步计划", {
    x: 1.22,
    y: 3.15,
    w: 6.8,
    h: 0.35,
    fontFace: "Microsoft YaHei",
    fontSize: 15,
    color: colors.sub
  });
  slide.addText("学生姓名：__________\n专业班级：__________\n指导教师：__________", {
    x: 1.2,
    y: 4.55,
    w: 4.8,
    h: 1.25,
    fontFace: "Microsoft YaHei",
    fontSize: 13.5,
    color: colors.text,
    breakLine: true,
    paraSpaceAfterPt: 7
  });
}

// 2 背景
{
  const slide = pptx.addSlide();
  baseSlide(slide, "1. 课题背景与研究目标");
  addBullets(slide, 0.75, 1.6, 7.4, 4.9, [
    "传统农产品销售环节多、信息不对称，农户和消费者两端都存在实际痛点。",
    "本课题希望做一个面向农产品销售场景的管理平台，覆盖商品、购物车、订单和数据统计等核心流程。",
    "项目采用前后端分离方式开发，前端使用 Vue，后端使用 Spring Boot，便于模块划分和后续维护。",
    "中期阶段的重点不是把所有功能做满，而是先把主要业务链路跑通。"
  ]);
  addCard(slide, 8.55, 1.95, 3.35, 1.95, "本课题想解决的问题", [
    "商品管理不够方便",
    "订单流程不够完整",
    "不同角色边界不清晰"
  ]);
  addCard(slide, 8.55, 4.2, 3.35, 1.95, "目前明确的目标", [
    "完成基础交易闭环",
    "实现角色权限控制",
    "提供基础数据看板"
  ]);
}

// 3 总体设计
{
  const slide = pptx.addSlide();
  baseSlide(slide, "2. 系统总体设计");
  addCard(slide, 0.75, 1.65, 3.6, 2.15, "前端层", [
    "Vue 2 + Element UI",
    "负责页面展示、表单交互、接口调用"
  ]);
  addCard(slide, 4.55, 1.65, 3.6, 2.15, "后端层", [
    "Spring Boot + Spring Security",
    "负责接口、权限、业务逻辑"
  ]);
  addCard(slide, 8.35, 1.65, 3.55, 2.15, "数据层", [
    "MyBatis + MySQL + Redis",
    "负责数据存储与缓存"
  ]);
  addCard(slide, 0.75, 4.1, 11.15, 2.15, "当前系统的主要业务模块", [
    "商品管理：商品信息维护、上下架、库存管理",
    "购物车管理：买家添加商品、修改数量、准备下单",
    "订单管理：支付、发货、收货、取消等状态流转",
    "权限与统计：管理员、卖家、买家三类角色，以及首页经营数据展示"
  ], { fill: "FAFAFA", titleColor: "3C3C3C" });
}

// 4 已完成工作
{
  const slide = pptx.addSlide();
  baseSlide(slide, "3. 中期阶段已完成的工作");
  addCard(slide, 0.75, 1.5, 3.6, 2.45, "后端部分", [
    "完成商品、购物车、订单等核心接口开发",
    "实现订单创建、库存扣减、状态修改等业务逻辑",
    "完成基于角色的数据访问控制"
  ]);
  addCard(slide, 4.55, 1.5, 3.6, 2.45, "前端部分", [
    "完成登录、首页、商品、购物车、订单等页面",
    "实现前后端接口联调",
    "完成基础表格、表单和弹窗交互"
  ]);
  addCard(slide, 8.35, 1.5, 3.55, 2.45, "数据库与环境", [
    "整理项目数据库脚本",
    "配置 MySQL、Redis、Maven、Node 环境",
    "完成本地运行和基础测试"
  ]);
  addCard(slide, 0.75, 4.35, 11.15, 2.0, "阶段性结果", [
    "目前系统主流程已经能够跑通：商品浏览 -> 加入购物车 -> 提交订单 -> 支付/发货/收货。",
    "管理员、卖家、买家三类用户在系统中的可见菜单和可操作数据存在区分。",
    "首页已经具备基础统计功能，可以展示销售额、订单数量、热销商品等信息。"
  ]);
}

// 5 进度说明
{
  const slide = pptx.addSlide();
  baseSlide(slide, "4. 当前进度说明");
  addCard(slide, 0.75, 1.75, 2.6, 3.8, "需求分析", ["已完成", "已明确系统角色、功能范围和主要业务流程"], { fill: "ECF8ED" });
  addCard(slide, 3.65, 1.75, 2.6, 3.8, "系统设计", ["已完成", "完成模块划分、数据库设计和接口结构整理"], { fill: "ECF8ED" });
  addCard(slide, 6.55, 1.75, 2.6, 3.8, "核心功能开发", ["基本完成", "主流程可运行，细节体验和异常处理仍在完善"], { fill: colors.warnBg, titleColor: colors.warn });
  addCard(slide, 9.45, 1.75, 2.45, 3.8, "论文与材料", ["进行中", "已形成开题报告，正在整理中期检查材料和论文初稿"], { fill: colors.warnBg, titleColor: colors.warn });
}

// 6 问题
{
  const slide = pptx.addSlide();
  baseSlide(slide, "5. 目前存在的问题");
  addBullets(slide, 0.75, 1.75, 7.3, 4.9, [
    "对 Spring Boot、Vue 这类框架的理解还在加深，很多功能最开始是边学边做。",
    "项目虽然主流程已经通了，但个别细节还不够完善，比如分类统计、页面细节优化、异常提示统一等。",
    "支付和物流部分目前还是偏模拟，离真实商业系统还有差距。",
    "论文部分需要尽快把系统实现内容转成规范文字，避免后期时间过于集中。"
  ]);
  addCard(slide, 8.5, 2.0, 3.4, 1.95, "自我判断", [
    "进度总体可控",
    "系统主体已经成型",
    "后续重点是完善和总结"
  ], { fill: "FAF6EE", titleColor: "8C6220" });
  addCard(slide, 8.5, 4.3, 3.4, 1.95, "接下来要补的点", [
    "论文内容整理",
    "细节测试与修正",
    "答辩材料准备"
  ], { fill: "FAF6EE", titleColor: "8C6220" });
}

// 7 计划
{
  const slide = pptx.addSlide();
  baseSlide(slide, "6. 下一步计划");
  addCard(slide, 0.75, 1.75, 3.6, 4.1, "功能完善", [
    "继续补充和优化商品、订单、统计模块细节",
    "完善页面交互和异常提示",
    "补做必要的测试和运行截图"
  ]);
  addCard(slide, 4.55, 1.75, 3.6, 4.1, "论文推进", [
    "按照系统分析、设计、实现、测试的结构整理正文",
    "把已有项目内容转化为论文表达",
    "同步修改格式、图表和参考文献"
  ]);
  addCard(slide, 8.35, 1.75, 3.55, 4.1, "答辩准备", [
    "准备项目演示流程",
    "梳理技术点和业务逻辑讲解",
    "提前准备老师可能提问的问题"
  ]);
}

// 8 总结
{
  const slide = pptx.addSlide();
  baseSlide(slide, "7. 阶段总结");
  slide.addShape(pptx.ShapeType.roundRect, {
    x: 0.9,
    y: 1.6,
    w: 10.85,
    h: 4.5,
    rectRadius: 0.08,
    fill: { color: "F8FBF7" },
    line: { color: colors.border, pt: 1.1 }
  });
  slide.addText("目前项目已经完成了中期阶段应有的主要工作，系统核心功能基本具备，能够体现课题的主要设计思路。", {
    x: 1.3,
    y: 2.25,
    w: 9.8,
    h: 0.8,
    fontFace: "Microsoft YaHei",
    fontSize: 18,
    bold: true,
    color: "303030",
    breakLine: true
  });
  slide.addText("接下来我的重点会放在两个方面：一是继续把系统细节完善好，二是把论文和汇报材料整理扎实。", {
    x: 1.3,
    y: 3.25,
    w: 9.6,
    h: 0.7,
    fontFace: "Microsoft YaHei",
    fontSize: 16,
    color: "444444"
  });
  slide.addText("整体来看，本课题能够按计划继续推进，后续目标是顺利完成论文、演示和正式答辩。", {
    x: 1.3,
    y: 4.1,
    w: 9.6,
    h: 0.7,
    fontFace: "Microsoft YaHei",
    fontSize: 16,
    color: "444444"
  });
  slide.addText("谢谢各位老师指导", {
    x: 1.3,
    y: 5.2,
    w: 3.5,
    h: 0.4,
    fontFace: "Microsoft YaHei",
    fontSize: 20,
    bold: true,
    color: "2A5A2F"
  });
}

const notes = `《基于 SpringBoot+Vue 的农产品销售平台》中期检查讲稿

第1页 封面
各位老师好，我的课题是《基于 SpringBoot+Vue 的农产品销售平台设计与实现》。下面我从课题背景、系统设计、目前进展、存在问题以及下一步计划几个方面做一个中期汇报。

第2页 课题背景与研究目标
这个课题主要是针对农产品销售过程中的一些实际问题，比如销售环节多、信息不够透明、管理方式比较分散等。我希望通过这个项目做一个比较完整的农产品销售平台，把商品管理、购物车、订单处理和数据统计这些核心功能串起来。中期阶段我的目标不是把系统做得特别复杂，而是先把主要流程跑通。

第3页 系统总体设计
系统采用前后端分离架构。前端使用 Vue 和 Element UI，负责页面展示和交互；后端使用 Spring Boot，负责接口、权限和业务逻辑；数据层使用 MySQL 和 Redis。当前系统主要包括商品管理、购物车管理、订单管理以及权限与统计四个部分。

第4页 中期阶段已完成的工作
到目前为止，后端已经完成了商品、购物车、订单这些核心接口开发，前端完成了登录、首页、商品、购物车和订单相关页面，并且已经完成前后端联调。数据库脚本和本地开发环境也已经整理好。现在系统的主流程基本可以跑通。

第5页 当前进度说明
从整体进度来看，需求分析和系统设计已经完成，核心功能开发基本完成，论文和材料整理还在进行中。也就是说，项目主体已经做出来了，现在更多是在细化、完善和总结。

第6页 目前存在的问题
目前遇到的问题主要有几方面。第一，我对 Spring Boot 和 Vue 的理解还在继续加深，开发过程中有比较明显的学习过程。第二，系统虽然能运行，但一些细节还需要继续优化。第三，像支付和物流这些部分目前还是模拟实现。第四，论文写作需要尽快跟上开发进度。

第7页 下一步计划
下一步我会继续完善系统细节，补充必要的测试和截图，同时把论文正文按规范整理出来。另外也会提前准备好项目演示流程和答辩中可能会被问到的技术点，避免后期时间太赶。

第8页 阶段总结
整体来说，我认为项目已经完成了中期阶段应该完成的主要内容，系统核心功能已经具备，课题可以继续按计划推进。后续重点是把功能做扎实、把论文写规范，为最终答辩做好准备。
`;

fs.writeFileSync(outputNotes, notes, "utf8");

pptx.writeFile({ fileName: outputPpt });
