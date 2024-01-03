package com.wxcp.server.price.service;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.wxcp.server.price.dao.*;
import com.wxcp.server.price.vo.StringUtils;
import com.wxcp.server.price.impl.*;
import com.wxcp.server.price.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
public class UndVehicleConfServiceImpl implements UndVehicleConfService {

    @Autowired
    private UndVehicleBasicConfService basicConfService;

    @Autowired
    private UndVehicleDetailConfService detailConfService;

    @Autowired
    private UndVehicleAttrService attrService;

    @Autowired
    private UndVehicleBasicExtService basicExtService;

    @Autowired
    private UndVehicleOptionalConfService optionalConfService;

    @Autowired
    private UndVehicleOptionalExtService optionalExtService;

    @Autowired
    private UndVehicleDetailExtService specialConfService;

    @Autowired
    private UndVehicleAttrMapper undVehicleAttrMapper;

    @Autowired
    private UndVehicleBasicConfMapper undVehicleBasicConfMapper;

    @Autowired
    private UndVehicleBasicExtMapper undVehicleBasicExtMapper;

    @Autowired
    private UndVehicleDetailConfMapper undVehicleDetailConfMapper;

    @Autowired
    private UndVehicleDetailExtMapper undVehicleDetailExtMapper;

    @Autowired
    private UndVehicleOptionalConfMapper undVehicleOptionalConfMapper;

    @Autowired
    private UndVehicleOptionalExtMapper undVehicleOptionalExtMapper;

    private final Map<String, String> attrMap = new HashMap<>();

    private final String pricePattern = "\\d+万|\\d+\\.\\d+万";

    private final String distancePattern = "运距.*?km";

    private final String speedPattern = "车速.*?km/h";

    private final String parenthesisPattern = "[\\u4E00-\\u9FA5]+：?(（\\d*(.\\d*)?万）)?(（(.*)）)(【.*】)?";

    private final String marketSegmentPattern = "(([一二三四五]|[1-9])(.|、))?((港口（集装箱）)?[[\\u4E00-\\u9FA5]、\\-/]+)(（.*）)?";

    private final String stereotypePattern = ".*((标载版)|(危运版)|(加强版)|(危运山区版)|(公路标准版)|(城建标载版)|(标准版)|(轻量化版)|(城建标准版)|(城建加强版)|(重柜版)|(标柜版)|(危运公路版)|(矿用加强版)|(超强版)|(轻柜版)|(复合版)|(空柜版)|(砂石版)).*";

    private final String versionPattern = ".*((经典版)|(菁英版)|(旗舰版)).*";

    private final String vanCar = "载货车";
    private final String suvCar = "越野车";
    private final String dumpCar = "自卸车";
    private final String tractorCar = "牵引车";
    private final String specialCar = "专用车";

//    @PostConstruct
//    private void init() {
//        List<UndVehicleAttr> list = attrService.list();
//        list.forEach(l -> attrMap.put(l.getConfKey(), l.getConfValue()));
//    }

    private boolean isEmptyRow(Row row) {
        if (row == null || row.toString().isEmpty()) {
            return true;
        } else {
            boolean isEmpty = true;
            for (int c = row.getFirstCellNum(); c < row.getLastCellNum(); c++) {
                Cell cell = row.getCell(c);
                if (cell == null) {
                    continue;
                }
                if (cell.getCellType() != CellType.BLANK) {
                    isEmpty = false;
                    break;
                }
            }
            return isEmpty;
        }
    }

    private String cellToString(XSSFCell cell) {
        if (cell == null) {
            return null;
        }
        if (cell.getCellType() != CellType.STRING) {
            cell.setCellType(CellType.STRING);
            return cell.getStringCellValue();
        }
        return cell.getStringCellValue();
    }

    private int getMergeRowNum(XSSFSheet sheet, Cell cell) {
        int mergeSize = 1;
        List<CellRangeAddress> mergedRegions = sheet.getMergedRegions();
        for (CellRangeAddress cellRangeAddress : mergedRegions) {
            if (cellRangeAddress.isInRange(cell)) {
                mergeSize = cellRangeAddress.getLastRow() - cellRangeAddress.getFirstRow() + 1;
                break;
            }
        }
        return mergeSize;
    }

    @Override
    @Transactional
    public void upload(List<MultipartFile> multipartFiles) {
        undVehicleBasicExtMapper.delete(new LambdaQueryWrapper<>());
        undVehicleBasicConfMapper.delete(new LambdaQueryWrapper<>());
        undVehicleDetailConfMapper.delete(new LambdaQueryWrapper<>());
        undVehicleDetailExtMapper.delete(new LambdaQueryWrapper<>());
        undVehicleOptionalConfMapper.delete(new LambdaQueryWrapper<>());
        undVehicleOptionalExtMapper.delete(new LambdaQueryWrapper<>());

        List<UndVehicleAttr> list = attrService.list();
        list.forEach(l -> attrMap.put(l.getConfKey(), l.getConfValue()));

        multipartFiles.forEach(file -> {
            String originalFilename = file.getOriginalFilename();
            String filename = originalFilename.substring(originalFilename.lastIndexOf("/") + 1);
            XSSFWorkbook workbook = null;
            try {
                workbook = new XSSFWorkbook(file.getInputStream());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            Map<XSSFSheet, List<List<XSSFRow>>> dataMap = splitData(workbook, filename);

            saveData(dataMap, filename);
        });


    }

    private void saveData(Map<XSSFSheet, List<List<XSSFRow>>> dataMap, String filename) {
        dataMap.forEach((sheet, data) -> {
            for (List<XSSFRow> rows : data) {
                log.info("save data 文件名，sheet名，" + filename + "，" + sheet.getSheetName());

                UndVehicleBasicConf basicConf = new UndVehicleBasicConf();
                basicConf.setFileName(filename);
                basicConf.setSheetName(sheet.getSheetName());

                XSSFRow row0 = rows.get(0);
                String cell0 = cellToString(row0.getCell(0));
                basicConf.setScenarios(cell0);
                if (StringUtils.isNotEmpty(cell0)) {
                    basicConf.setHaulDistance(matchStr(distancePattern, cell0, 0));
                    basicConf.setSpeed(matchStr(speedPattern, cell0, 0));
                    basicConf.setMarketSegment(matchStr(marketSegmentPattern, cell0, 4));
                }

                XSSFRow row1 = rows.get(1);
                basicConf.setFuelType(cellToString(row1.getCell(0)));
                XSSFRow row2 = rows.get(2);
                String cell2 = cellToString(row2.getCell(0));
                basicConf.setVehicleCharacter(cell2);
                if (StringUtils.isNotEmpty(cell2)) {
                    basicConf.setStereotype(matchStr(stereotypePattern, cell2, 1));
                    basicConf.setVersion(matchStr(versionPattern, cell2, 1));
                }

                basicConfService.save(basicConf);
                Long basicId = basicConf.getId();

                //基础配置
                XSSFRow row3 = rows.get(3);
                String basicConfText = cellToString(row3.getCell(3));
                setBasicConfText(basicId, basicConfText);

                //选装版型
                XSSFRow row4 = rows.get(4);
                XSSFCell cell4 = row4.getCell(0);
                int mergeRowNum = setOptionalType(sheet, rows, basicId, cell4);

                //选装配置
                XSSFRow row5 = rows.get(4 + mergeRowNum);
                String optionalConf = cellToString(row5.getCell(3));
                setOptionalConfText(basicId, optionalConf, sheet);

                //详细属性
                setDetailConf(sheet, rows, basicId, mergeRowNum);
            }
        });
    }

    private void setDetailConf(XSSFSheet sheet, List<XSSFRow> rows, Long basicId, int mergeRowNum) {
        int step;
        for (int i = 6 + mergeRowNum; i < rows.size(); i = i + step) {
            XSSFRow row = rows.get(i);
            UndVehicleDetailConf detailConf = new UndVehicleDetailConf();
            setProperties(basicId, row, detailConf);
            detailConfService.save(detailConf);

            XSSFCell cell = row.getCell(0);
            int mergeRowNum1 = getMergeRowNum(sheet, cell);

            step = mergeRowNum1;

            List<UndVehicleDetailExt> list = new ArrayList<>();
            for (int j = 0; j < mergeRowNum1; j++) {
                UndVehicleDetailExt specialConf = new UndVehicleDetailExt();
                specialConf.setDetailId(detailConf.getId());

                XSSFRow rowJ = rows.get(i + j);
                String announcementType = cellToString(rowJ.getCell(15));
                specialConf.setAnnouncementType(announcementType);
                String topSort = cellToString(rowJ.getCell(16));
                specialConf.setTopSort(topSort);
                String containerSize = cellToString(rowJ.getCell(17));
                specialConf.setContainerSize(containerSize);
                String announcement = cellToString(rowJ.getCell(22));
                specialConf.setAnnouncement(announcement);
                String threeC = cellToString(rowJ.getCell(23));
                specialConf.setThreeC(threeC);
                String environmentalProtection = cellToString(rowJ.getCell(24));
                specialConf.setEnvironmentalProtection(environmentalProtection);
                String modelCode = cellToString(rowJ.getCell(25));
                specialConf.setModelCode(modelCode);
                list.add(specialConf);
            }
            specialConfService.saveBatch(list);
        }
    }

    private void setOptionalConfText(Long basicId, String optionalConf, Sheet sheet) {
        if (StringUtils.isEmpty(optionalConf)) {
            return;
        }

        String[] optionalArray = optionalConf.split("\n");

        for (int i = 0; i < optionalArray.length; i++) {
            UndVehicleOptionalConf conf = new UndVehicleOptionalConf();
            conf.setBasicId(basicId);
            conf.setOptionalType(2);
            String line = optionalArray[i];

            String price = matchStr(pricePattern, line, 0);
            conf.setPrice(price);
            conf.setContent(line);
            conf.setOptionalName("选装配置集合");
            if (i != optionalArray.length - 1) {
                String optionalName = beforeBracket(line);
                conf.setOptionalName(optionalName);
            }
            optionalConfService.save(conf);

            List<UndVehicleOptionalExt> list = new ArrayList<>();

            if (i != optionalArray.length - 1) {
                setOptionalExt(conf, line, list);
            }

            if (i == optionalArray.length - 1) {
//                String[] subOptionalText = line.split("、");
                List<String> subOptionalText = optionalConfSplit(line, sheet.getSheetName());
                for (String s : subOptionalText) {
                    UndVehicleOptionalExt ext = getOptionalExt(conf, s);
                    list.add(ext);
                }
            }
            optionalExtService.saveBatch(list);
        }
    }

    private UndVehicleOptionalExt getOptionalExt(UndVehicleOptionalConf conf, String s) {
        UndVehicleOptionalExt ext = new UndVehicleOptionalExt();
        ext.setOptionalId(conf.getId());
        ext.setExtValue(s);
        String extKey = attrMap.get(s);
        if (StringUtils.isNotEmpty(extKey)) {
            ext.setExtKey(extKey);
        }
        return ext;
    }

    private int setOptionalType(XSSFSheet sheet, List<XSSFRow> rows, Long basicId, XSSFCell cell4) {
        int mergeRowNum = getMergeRowNum(sheet, cell4);

        for (int i = 0; i < mergeRowNum; i++) {
            UndVehicleOptionalConf optionalConf = new UndVehicleOptionalConf();
            optionalConf.setBasicId(basicId);
            optionalConf.setOptionalType(1);

            XSSFRow mergeRow4 = rows.get(4 + i);
            String stringCellValue3 = cellToString(mergeRow4.getCell(3));
            if (StringUtils.isEmpty(stringCellValue3)) {
                continue;
            }
            String price = matchStr(pricePattern, stringCellValue3, 0);
            optionalConf.setPrice(price);
            String content = cellToString(mergeRow4.getCell(6));
            optionalConf.setContent(content);
            String optionalName = beforeBracket(content);
            optionalConf.setOptionalName(optionalName);
            optionalConfService.save(optionalConf);

            List<UndVehicleOptionalExt> list = new ArrayList<>();
            setOptionalExt(optionalConf, content, list);
            optionalExtService.saveBatch(list);
        }

        return mergeRowNum;
    }

    private void setOptionalExt(UndVehicleOptionalConf optionalConf, String content, List<UndVehicleOptionalExt> list) {
        String matchStr = matchStr(parenthesisPattern, content, 4);
        String[] sub = matchStr.split("、");
        if (sub.length == 1) {
            sub = matchStr.split("\\+");
        }
        for (String s : sub) {
            UndVehicleOptionalExt ext = getOptionalExt(optionalConf, s);
            list.add(ext);
        }
    }

    private String beforeBracket(String content) {
        if (StringUtils.isEmpty(content)) {
            return content;
        }
        String optionalName = "";
        int index = content.indexOf("（");
        if (index != -1) {
            optionalName = content.substring(0, index);
        }
        return optionalName;
    }

    private void setBasicConfText(Long basicId, String basicConfText) {
        if (StringUtils.isEmpty(basicConfText)) {
            return;
        }

        List<UndVehicleBasicExt> list = new ArrayList<>();
        String[] basicConfArray = basicConfText.split("、");
        for (String s : basicConfArray) {
            UndVehicleBasicExt ext = new UndVehicleBasicExt();
            ext.setBasicId(basicId);
            ext.setExtValue(s);
            String extKey = attrMap.get(s);
            if (StringUtils.isNotEmpty(extKey)) {
                ext.setExtKey(extKey);
            }
            list.add(ext);
        }
        basicExtService.saveBatch(list);
    }

    private void setProperties(Long basicId, XSSFRow cells, UndVehicleDetailConf detailConf) {
        detailConf.setBasicId(basicId);
        String salesCode = cellToString(cells.getCell(1));
        detailConf.setSalesCode(salesCode);
        if (StringUtils.isNotEmpty(salesCode) && salesCode.contains("/")) {
            String[] salesCodes = salesCode.split("/");
            if (StringUtils.isNotEmpty(salesCodes)) {
                detailConf.setCarSeries(salesCodes[0]);
                detailConf.setDriver(salesCodes[1]);
            }
        }

        String designCarModel = cellToString(cells.getCell(2));
        detailConf.setDesignCarModel(designCarModel);
        if (StringUtils.isNotEmpty(designCarModel)) {
            char c = designCarModel.charAt(2);
            String vehicleModel = "";
            switch (Character.getNumericValue(c)) {
                case 1:
                    vehicleModel = vanCar;
                    break;
                case 2:
                    vehicleModel = suvCar;
                    break;
                case 3:
                    vehicleModel = dumpCar;
                    break;
                case 4:
                    vehicleModel = tractorCar;
                    break;
                case 5:
                    vehicleModel = specialCar;
                    break;
            }
            detailConf.setVehicleModel(vehicleModel);
        }

        detailConf.setEngineType(cellToString(cells.getCell(3)));
        detailConf.setMotorPower(cellToString(cells.getCell(4)));
        detailConf.setSettlementPrice(cellToString(cells.getCell(5)));
        detailConf.setVariator(cellToString(cells.getCell(6)));
        detailConf.setVehicleBridge(cellToString(cells.getCell(7)));
        detailConf.setCarframe(cellToString(cells.getCell(8)));
        detailConf.setWheelBase(cellToString(cells.getCell(9)));
        detailConf.setFrontSuspension(cellToString(cells.getCell(10)));
        detailConf.setRearSuspension(cellToString(cells.getCell(11)));
        detailConf.setTyre(cellToString(cells.getCell(12)));
        detailConf.setSubdivisionCondition(cellToString(cells.getCell(13)));
        detailConf.setPowerMatchingOption(cellToString(cells.getCell(14)));

        detailConf.setPowerBattery(cellToString(cells.getCell(18)));
        detailConf.setOptionalBatterySystem(cellToString(cells.getCell(19)));
        detailConf.setExemption(cellToString(cells.getCell(20)));
        detailConf.setOperatingStandard(cellToString(cells.getCell(21)));
    }

    private Map<XSSFSheet, List<List<XSSFRow>>> splitData(XSSFWorkbook workbook, String filename) {
        Map<XSSFSheet, List<List<XSSFRow>>> dataMap = new LinkedHashMap<>();

        int activeSheetIndex = workbook.getNumberOfSheets();
        //遍历每个sheet
        for (int i = 0; i < activeSheetIndex; i++) {
            XSSFSheet sheet = workbook.getSheetAt(i);

            log.info("split data，文件名，sheet名，" + filename + "，" + sheet.getSheetName());

            //去除最后的所有空行
            while (isEmptyRow(sheet.getRow(sheet.getLastRowNum()))) {
                sheet.removeRow(sheet.getRow(sheet.getLastRowNum()));
            }

            List<List<XSSFRow>> data = new ArrayList<>();
            List<XSSFRow> rows = new ArrayList<>(15);
            //遍历每一行
            for (int j = 1; j < sheet.getPhysicalNumberOfRows(); j++) {
                XSSFRow row = sheet.getRow(j);
                //空行，代表一组数据完结
                if (isEmptyRow(row) && rows.size() > 3) {
                    data.add(rows);
                    rows = new ArrayList<>(15);
                } else {
                    rows.add(row);
                }
                //最后一行，读取完毕
                if (j == sheet.getPhysicalNumberOfRows() - 1) {
                    data.add(rows);
                }
            }
            dataMap.put(sheet, data);
        }
        return dataMap;
    }

    private String matchStr(String pattern, String str, Integer group) {
        String res = "";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(str);
        boolean b = m.find();
        if (b) {
            res = m.group(group);
        }
        return res;
    }

    private String lastParenthesis(String str) {
        if (StringUtils.isEmpty(str)) {
            return str;
        }
        String res = "";
        int lastOpenParenthesis = str.lastIndexOf('（');
        int lastCloseParenthesis = str.lastIndexOf('）');
        if (lastOpenParenthesis != -1 && lastCloseParenthesis > lastOpenParenthesis) {
            res = str.substring(lastOpenParenthesis + 1, lastCloseParenthesis);
        }
        return res;
    }

    public List<String> optionalConfSplit(String input, String sheetName) {
        log.info("sheet name is " + sheetName);
        int nParens = 0;
        int start = 0;
        List<String> result = new ArrayList<>();
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c == '、') {
                if (nParens == 0) {
                    result.add(input.substring(start, i));
                    start = i + 1;
                }
            } else if (c == '（' || c == '(') {
                nParens++;
            } else if (c == '）' || c == ')') {
                nParens--;
                if (nParens < 0) {
                    throw new IllegalArgumentException("Unbalanced parenthesis at offset #" + i);
                }
            }
        }
        if (nParens > 0) {
            throw new IllegalArgumentException("Missing closing parenthesis");
        }
        result.add(input.substring(start));
        return result;
    }
}
