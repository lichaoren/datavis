# econflow — 部门资金流向（日更新）

第一步：用**美国财政部每日现金报表 (Daily Treasury Statement, DTS)** 追踪「钱在联邦政府与各部门之间怎么流」，按任意时间窗口查看。

为什么从 DTS 开始：它是唯一**每日更新**、且每笔都能对应到交易对手部门的官方资金流数据（每笔存款 = 某部门 → 政府，每笔支出 = 政府 → 某部门）。家庭 ↔ 企业 ↔ 海外之间的流量（Fed Z.1）只有季度数据，后续再接。

## 用法

纯 Python 标准库，无依赖（Python ≥ 3.10）。

```bash
cd econflow
python -m econflow update      # 首次从 2022-01-01 回填；之后只抓新日期，然后导出 data/flows.json
python -m econflow serve       # 打开 http://localhost:8000/web/
python -m econflow unmapped    # 列出仍归为 OTHER 的科目（按金额排序），用于完善映射
python -m econflow export      # 修改 sectors.py 后重新归类，无需重新抓取
python -m unittest discover -s tests
```

日更新（DTS 约美东 16:00 发布前一工作日数据）：

```cron
30 22 * * 1-5  cd /path/to/datavis/econflow && python3 -m econflow update >> data/update.log 2>&1
```

## 页面

- **时间窗口**：最新1天 / 5天 / 1月 / 3月 / 月初至今 / 年初至今 / 1年 / 全部，或自定义起止日期
- **桑基图**：左 = 各部门付给政府，右 = 政府付给各部门，TGA 现金增减作为平衡项（流入 ≡ 流出）
- **部门净头寸表**：净注入 = 从政府收到 − 付给政府；Σ净注入 + ΔTGA ≡ 0
- **每日净注入**：按部门堆叠（窗口 >130 天按周，>600 天按月汇总）
- **明细科目**：点击桑基图节点按部门筛选
- **国债口径**：净发行（默认）/ 总额（含短债滚动，体量极大）/ 剔除
- **守恒校验**：流量推算 ΔTGA vs 报表 TGA 余额变化，残差 ≠ 0 说明有科目被漏掉或误排除

## 部门归类

`econflow/sectors.py` 中按顺序的正则规则，首个匹配生效：

| 部门 | 典型科目 |
|---|---|
| HH 家庭 | 个税/FICA 预扣；社保、Medicare、SNAP、退伍军人、个人退税、联邦薪资 |
| FIRM 企业 | 企业所得税、关税/消费税、FUTA；国防采购、企业退税 |
| SL 州/地方政府 | Medicaid 等对州拨款、公路/交通拨款 |
| FIN 金融/债券持有人 | 国债发行/赎回、国债利息（含海外持有人，DTS 无法拆分） |
| FED 美联储 | 美联储利润上缴 |
| ROW 海外 | 国务院、对外援助、IMF |
| OTHER | 未匹配，用 `unmapped` 查看 |

归类是启发式的 SFC 口径（如 Medicare 视为对家庭的实物转移），不是收款方字面意义。

## 文件

```
econflow/dts.py      Fiscal Data API 抓取与解析（Table II 存取款、TGA 余额）
econflow/store.py    SQLite（data/econflow.db，原始科目行，幂等 upsert）
econflow/sectors.py  科目 → 部门映射
econflow/export.py   导出 data/flows.json（按日 × 科目）
web/index.html       D3 前端，窗口聚合在浏览器内完成
```
