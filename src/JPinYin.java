import java.io.*;
import java.util.*;

public class JPinYin {

    public static int TONE_UNICODE = 0;
    public static int TONE_NUMBER = 1;
    public static int TONE_NONE = 2;

    private static int IDX_DICT_SURNAME = 0 ;
    private static String DICTS_FILE_SURNAMES =  "surnames";
    private static String DICTS_FILE_WORDS =  "words_";
    private static String DICTS_FILE_USERWORDS =  "users_";

    private String dictsPath;
    private int dictWordMaxLen =0;

    private Map<Integer,HashMap<String,String>> dicts = null;

    public Map<Character,Set<String>> dictWord = null;

    public JPinYin(String dictsPath){
        this.dictsPath = dictsPath;
        loadDictsFromFile();

    }

    /**
     * 加载词典文件
     */
    private void loadDictsFromFile(){
        dicts = new HashMap<Integer, HashMap<String, String>>();
        dictWord = new HashMap<>();

         //状态姓名多音字词典
        loadDictsFromFile(new File(dictsPath,DICTS_FILE_SURNAMES),true);

        //装在内置词典
        loadDictsFromFiles(dictsPath,DICTS_FILE_WORDS,false);

        //装在用户自定义词典
        loadDictsFromFiles(dictsPath,DICTS_FILE_USERWORDS,false);

        reportDictInfo();

    }

    private void reportDictInfo(){
        int dictSize = 0;
        for (Map m : dicts.values() ){
            dictSize += m.size();
        }
        System.out.println(String.format("load dicts finished. phrase count=%d, words count=%d .",dictSize,dictWord.size()));
    }

    /**
     * 加载词典文件
     * @param path
     * @param nameStartWith
     * @param isSurname
     */
    private void loadDictsFromFiles(String path, String nameStartWith, boolean isSurname){
        TreeSet<File> fileList = new TreeSet<File>(new Comparator<File>(){
            @Override
            public int compare(File o1, File o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });

        File[] files = new File(path).listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.startsWith(nameStartWith);
            }
        });

        for (int i=0;i<files.length;i++){
            fileList.add(files[i]);
        }

        Iterator<File> itr = fileList.iterator();
        while(itr.hasNext()){
            loadDictsFromFile(itr.next(),isSurname);
        }

    }

    /**
     * 加载词典文件
     * @param file
     * @param isSurname
     */
    public void loadDictsFromFile(File file, boolean isSurname){
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(file),"UTF-8"));
            String line = null;
            while((line=br.readLine())!=null){
                if (line.indexOf("'")<0){
                    continue;
                }else{
                    line = line.replace(',',' ').replace('\'',' ').trim();
                    String[] word = line.split("=>");
                    String wordChinese = word[0].trim();
                    String wordPinyin = word[1].trim();

                    //多音字姓名文件
                    if (isSurname){
                        HashMap<String,String> m = dicts.get(IDX_DICT_SURNAME);
                        if (m==null){
                            m = new HashMap<String,String>();
                            dicts.put(IDX_DICT_SURNAME,m);
                        }
                        m.put(wordChinese,wordPinyin);
                    }else{
                        HashMap<String,String> m = dicts.get(wordChinese.length());
                        if (m==null){
                            m = new HashMap<String,String>();
                            dicts.put(wordChinese.length(),m);
                        }
                        m.put(wordChinese,wordPinyin);

                        if (wordChinese.length()> dictWordMaxLen){
                            dictWordMaxLen = wordChinese.length();
                        }
                    }

                    //构建词典
                    String[] pinyin = wordPinyin.split("\t");
                    for(int j=0; j<wordChinese.length(); j++){
                        Set set = dictWord.get(wordChinese.charAt(j));
                        if (set==null){
                            set = new HashSet<String>();
                            dictWord.put(wordChinese.charAt(j),set);
                        }
                        set.add(pinyin[j]);

                    }

                }
            }

            System.out.println("load dict file ["+file.getAbsolutePath()+"] finished.");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br!=null){
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 转换单个字符
     * @param c
     * @param toneType
     * @return
     */
    public String[] convertChar(char c, int toneType){
        return convertChar(c,toneType,false,false);
    }

    /**
     * 转换单个字符，可指定只有首字母，是否的大写第一个字母
     * @param c
     * @param toneType
     * @param onlyFirstChar
     * @param firstCharUpperCase
     * @return
     */
    public String[] convertChar(char c, int toneType, boolean onlyFirstChar, boolean firstCharUpperCase){
        Set<String> set = dictWord.get(c);
        if (set==null){
            return null;
        }
        List<String> list = new ArrayList<>();
        for (String s: set){
            list.add(formatTone(s,toneType,"",onlyFirstChar,firstCharUpperCase));
        }
        return list.toArray(new String[0]);
    }

    /**
     * 转换为链接字符串，只保留中文的拼音，无音调，默认使用中短线。
     * @param text
     * @return
     */
    public String convertLink(String text){
        return convertLinkUseShortMiddleLine(text);
    }

    /**
     * 转换为链接字符串，只保留中文的拼音，无音调，默认使用中短线。
     * @param text
     * @return
     */
    public String convertLinkUseShortMiddleLine(String text){
        return convertLink(text,"-");
    }

    /**
     * 转换为链接字符串，只保留中文的拼音，无音调，默认使用下划线。
     * @param text
     * @return
     */
    public String convertLinkUseUnderLine(String text){
        return convertLink(text,"_");
    }

    /**
     * 转换为链接字符串，只保留中文的拼音，无音调，默认使用点。
     * @param text
     * @return
     */
    public String convertLinkUseDot(String text){
        return convertLink(text,".");
    }

    /**
     * 转换为链接字符串，指定分隔符，只保留中文的拼音，无音调。
     * @param text
     * @param pinyinSeparator
     * @return
     */
    public String convertLink(String text, String pinyinSeparator){
        return convertTextInner(text,TONE_NONE,pinyinSeparator,pinyinSeparator,false,false,false, true);
    }

    /**
     *
     * @param text
     * @param pinyinSeparator
     * @param onlyFirstChar
     * @param firstCharUpperCase
     * @return
     */
    public String convertLink(String text, String pinyinSeparator, boolean onlyFirstChar, boolean firstCharUpperCase){
        return convertTextInner(text,TONE_NONE,pinyinSeparator,pinyinSeparator,false,onlyFirstChar,firstCharUpperCase, true);
    }

    /**
     * 转换中文句子为拼音字符串，指定音调类型
     * @param text
     * @param toneType
     * @return
     */
    public String convertSentence(String text, int toneType){
        return convertTextBase(text,toneType,"","",false,false,false);
    }

    /**
     * 转换中文字符为拼音字符串，指定音调类型、拼音分隔符
     * @param text
     * @param toneType
     * @param pinyinSeparator
     * @return
     */
    public String convert(String text, int toneType, String pinyinSeparator){
        return convertTextBase(text,toneType,pinyinSeparator,pinyinSeparator,false,false,false);
    }

    /**
     * 转换中文字符为拼音字符串，指定音调类型、拼音分隔符、只保留首字母、首字母是否大写
     * @param text
     * @param toneType
     * @param pinyinSeparator
     * @param onlyFirstChar
     * @param firstCharUpperCase
     * @return
     */
    public String convert(String text, int toneType, String pinyinSeparator , boolean onlyFirstChar, boolean firstCharUpperCase){
        return convertTextBase(text,toneType,pinyinSeparator,pinyinSeparator,false,onlyFirstChar,firstCharUpperCase);
    }

    public String convert(String text, int toneType, String pinyinSeparator, String phraseSeparator, boolean onlyFirstChar, boolean firstCharUpperCase){
        return convertTextBase(text,toneType,pinyinSeparator,phraseSeparator,false,onlyFirstChar,firstCharUpperCase);
    }

    private String convertTextBase(String text, int toneType, String pinyinSeparator, String phraseSeparator, boolean prevWordIsNumeralWord, boolean onlyFirstChar, boolean firstCharUpperCase){
        return convertTextInner( text,  toneType,  pinyinSeparator,  phraseSeparator,  prevWordIsNumeralWord,  onlyFirstChar,  firstCharUpperCase, false);
    }

    private String convertTextInner(String text, int toneType, String pinyinSeparator, String phraseSeparator, boolean prevWordIsNumeralWord, boolean onlyFirstChar, boolean firstCharUpperCase, boolean onlyHanZi){
        if (text==null || text.length()==0){
            return "";
        }

        if (onlyHanZi && !isHanZi(text.charAt(0))){
            return convertTextInner(text.substring(1),toneType, pinyinSeparator,phraseSeparator,prevWordIsNumeralWord,onlyFirstChar,firstCharUpperCase,onlyHanZi);
        }

        //如果不包含汉字，则返回原样
        //if (!text.matches("\\p{IsHan}")){
        //    return text;
        //}

        String phrase = null;
        String pinyin = null;

        int len = text.length()>= dictWordMaxLen ? dictWordMaxLen : text.length();
        HashMap<String,String> m = null;
        for ( int i=len; i>0 ;i--){
            phrase = text.substring(0,i);
            m = dicts.get(phrase.length());
            if (m==null){
                continue;
            }
            pinyin = m.get(phrase);
            if (pinyin!=null){
                break;
            }
        }

        //如果量词前面是数词，则使用量词拼音表
        if (prevWordIsNumeralWord && phrase.length()==1){
            String measureWordPinyin = findMeasureWordPinyin(phrase);
            if (measureWordPinyin!=null){
                pinyin = measureWordPinyin;
            }
        }

        //标记当前词的最后一个是否为数词
        if (isNumeralWord(phrase.charAt(phrase.length()-1))){
            prevWordIsNumeralWord = true;
        }else{
            prevWordIsNumeralWord = false;
        }

        //中文符号处理
        if (pinyin==null || phrase.length()==1){
            int c = phrase.charAt(0);
            for (int i=0; i<punctuations.length ; i++){
                if (c==punctuations[i][0]){
                    pinyin=Character.toString(punctuations[i][1]);
                    break;
                }
            }
        }

        //没找到, 原样输出
        if (pinyin==null){
            pinyin = phrase;
        }

        String substr = convertTextInner(text.substring(phrase.length()),toneType, pinyinSeparator,phraseSeparator,prevWordIsNumeralWord,onlyFirstChar,firstCharUpperCase,onlyHanZi);

        if (substr.length()==0){
            return formatTone(pinyin,toneType,pinyinSeparator,onlyFirstChar,firstCharUpperCase);
        }else{
            return formatTone(pinyin,toneType,pinyinSeparator,onlyFirstChar,firstCharUpperCase) + phraseSeparator+ substr;
        }
    }

    //TEST OK
    public String convertName(String name, int toneType) {
        return convertName(name,toneType,false, true);
    }

    //TEST OK
    public String convertName(String name, int toneType, boolean onlyFirstChar, boolean firstCharUpperCase){
        if (name==null || name.trim().length()==0){
            return name;
        }

        //如果不包含汉字，则返回原样
        //if (!name.matches("\\p{IsHan}")){
        //    return name;
        //}

        int nameMaxLen = 1;
        if (name.length()>2) {
            nameMaxLen = 2;
        }

        String surname = null;
        String pinyin = null;

        for (int i=nameMaxLen; i>0 ; i--) {
            surname = name.substring(0,i);
            pinyin = dicts.get(IDX_DICT_SURNAME).get(surname);
            if (pinyin!=null){
                break;
            }
        }

        if (pinyin==null){
            HashMap<String,String> m = dicts.get(surname.length());
            if (m !=null){
                pinyin = m.get(surname.length());
            }
        }

        return formatTone(pinyin,toneType,"",onlyFirstChar,firstCharUpperCase)+ "" +
                convertTextBase(name.substring(surname.length()),toneType,"","",false,onlyFirstChar,firstCharUpperCase);
    }

    //TEST OK
    public String formatTone(String pinyinUnicode, int toneType){
        return formatTone(pinyinUnicode,toneType,null,false,false);
    }

    //TEST OK
    public String formatTone(String pinyinUnicode, int toneType, String pinyinSeparator, boolean onlyFirstChar, boolean firstCharUpperCase){
        if (pinyinUnicode==null||pinyinUnicode.length()==0){
            return "";
        }

        //只保留首字母
        if (onlyFirstChar) {
            toneType = TONE_NONE;
        }

        String[] pinyins = pinyinUnicode.split("\t");

        if (pinyinSeparator==null){
            pinyinSeparator="\t";
        }

        StringBuilder ret = new StringBuilder();
        for (String py : pinyins){
            if (firstCharUpperCase || onlyFirstChar){
                String firstChar = Character.toString(py.charAt(0));
                if (firstCharUpperCase) {
                    firstChar = firstChar.toUpperCase();
                }

                if (onlyFirstChar) {
                    py = firstChar;
                }else {
                    py = firstChar + py.substring(1);
                }
            }

            ret.append(formatToneForOneWord(py,toneType)).append(pinyinSeparator);
        }
        return ret.toString().substring(0,ret.length()-pinyinSeparator.length());
    }

    //TEST OK
    public String formatToneForOneWord(String pinyinUnicode, int toneType){
        if (pinyinUnicode==null||pinyinUnicode.length()==0){
            return "";
        }

        if (toneType==TONE_UNICODE){
            return pinyinUnicode;
        }
        char c;
        StringBuilder ret = new StringBuilder();
        pinyinUnicode = pinyinUnicode.trim();

        char tone_number='0';
        for (int i=0;i<pinyinUnicode.length();i++){
            c = pinyinUnicode.charAt(i);

            if (c=='\t'||c==' '||c=='\'' || c==-1 ){
                if (tone_number!='0'){
                    ret.append(tone_number);
                    tone_number='0';
                }
                ret.append(c);
                continue;
            }

            boolean found = false;
            for (int j=0; j<toneDict.length; j++){
                if (c==toneDict[j][0]){
                    ret.append(toneDict[j][1]);
                    if (toneType==TONE_NUMBER){
                        tone_number=toneDict[j][2];
                    }
                    found = true;
                    break;
                }
            }

            if (!found){
                ret.append(c);
            }
        }
        if (tone_number!='0'){
            ret.append(tone_number);
            tone_number='0';
        }

        return ret.toString();
    }

    private boolean isHanZi(char c){
        //\u3400-\u4DB5,\u4E00-\u9FA5,\u9FA6-\u9FBB,\uF900-\uFA2D,\uFA30-\uFA6A,\uFA70-\uFAD9
        if ((c>=0x3400 && c<=0x4DB5 )
                || (c>=0x3400 && c<=0x4DB5 )
                || (c>=0x4E00 && c<=0x9FA5 )
                || (c>=0x9FA6 && c<=0x9FBB )
                || (c>=0xF900 && c<=0xFA2D )
                || (c>=0xFA30 && c<=0xFA6A )
                || (c>=0xFA70 && c<=0xFAD9 )) {
            return true;
        }else{
            return false;
        }
    }

    private boolean isNumeralWord(char c){
        for(int i = 0; i< numeralWords.length; i++){
            if (c== numeralWords[i]){
                return true;
            }
        }
        return false;
    }

    private String findMeasureWordPinyin(String c){
        for(int i = 0; i< measureWords.length; i++){
            if (measureWords[i][0].equalsIgnoreCase(c)){
                return measureWords[i][1];
            }
        }
        return null;
    }


    public String formatToUnicodeTone(String pinyinToneNumber){
        if (!pinyinToneNumber.matches("[a-zA-Z]+[1-4]*")){
            return "";
        }

        //tang4  ta4ng
        //wai3  wa3i

        char c = pinyinToneNumber.charAt(pinyinToneNumber.length()-1);

        char toneNumber = '0';
        if (c>='1' && c<='4'){
            toneNumber = c;
            pinyinToneNumber = pinyinToneNumber.substring(0,pinyinToneNumber.length()-1);
        }

        int idx = -1;
        int idxU=pinyinToneNumber.indexOf("u");
        int idxE=pinyinToneNumber.indexOf("e");
        int idxO=pinyinToneNumber.indexOf("o");
        int idxI=pinyinToneNumber.indexOf("i");
        if ((idx=pinyinToneNumber.indexOf("a"))>=0){
            if (c=='1'){
                pinyinToneNumber = pinyinToneNumber.replace('a','ā');
            }else if (c=='2'){
                pinyinToneNumber = pinyinToneNumber.replace('a','á');
            }else if (c=='3'){
                pinyinToneNumber = pinyinToneNumber.replace('a','ǎ');
            }else if (c=='4'){
                pinyinToneNumber = pinyinToneNumber.replace('a','à');
            }

        }else if ((idx=pinyinToneNumber.indexOf("e"))>=0 && (idx<idxU||idxU<0)){
            if (c=='1'){
                pinyinToneNumber = pinyinToneNumber.replace('e','ē');
            }else if (c=='2'){
                pinyinToneNumber = pinyinToneNumber.replace('e','é');
            }else if (c=='3'){
                pinyinToneNumber = pinyinToneNumber.replace('e','ě');
            }else if (c=='4'){
                pinyinToneNumber = pinyinToneNumber.replace('e','è');
            }

        }else if ((idx=pinyinToneNumber.indexOf("o"))>=0 && (idxI<0)){
            if (c=='1'){
                pinyinToneNumber = pinyinToneNumber.replace('o','ō');
            }else if (c=='2'){
                pinyinToneNumber = pinyinToneNumber.replace('o','ó');
            }else if (c=='3'){
                pinyinToneNumber = pinyinToneNumber.replace('o','ǒ');
            }else if (c=='4'){
                pinyinToneNumber = pinyinToneNumber.replace('o','ò');
            }
        }else if ((idx=pinyinToneNumber.indexOf("i"))>=0){
            if (idxU>idx){
                if (c=='1'){
                    pinyinToneNumber = pinyinToneNumber.replace('u','ū');
                }else if (c=='2'){
                    pinyinToneNumber = pinyinToneNumber.replace('u','ú');
                }else if (c=='3'){
                    pinyinToneNumber = pinyinToneNumber.replace('u','ǔ');
                }else if (c=='4'){
                    pinyinToneNumber = pinyinToneNumber.replace('u','ù');
                }


            }else{
                if (idxO>idx){
                    if (c=='1'){
                        pinyinToneNumber = pinyinToneNumber.replace('o','ō');
                    }else if (c=='2'){
                        pinyinToneNumber = pinyinToneNumber.replace('o','ó');
                    }else if (c=='3'){
                        pinyinToneNumber = pinyinToneNumber.replace('o','ǒ');
                    }else if (c=='4'){
                        pinyinToneNumber = pinyinToneNumber.replace('o','ò');
                    }
                }else {
                    if (c == '1') {
                        pinyinToneNumber = pinyinToneNumber.replace('i', 'ī');
                    } else if (c == '2') {
                        pinyinToneNumber = pinyinToneNumber.replace('i', 'í');
                    } else if (c == '3') {
                        pinyinToneNumber = pinyinToneNumber.replace('i', 'ǐ');
                    } else if (c == '4') {
                        pinyinToneNumber = pinyinToneNumber.replace('i', 'ì');
                    }
                }
            }
        }else if ((idx=pinyinToneNumber.indexOf("u"))>=0){
            if (idxE>idx){
                if (pinyinToneNumber.indexOf("j")>=0 ||pinyinToneNumber.indexOf("q")>=0 || pinyinToneNumber.indexOf("x")>=0 || pinyinToneNumber.indexOf("y")>=0 ){

                }else {
                    pinyinToneNumber = pinyinToneNumber.replace('u', 'ü');
                }

                if (c=='1'){
                    pinyinToneNumber = pinyinToneNumber.replace('e','ē');
                }else if (c=='2'){
                    pinyinToneNumber = pinyinToneNumber.replace('e','é');
                }else if (c=='3'){
                    pinyinToneNumber = pinyinToneNumber.replace('e','ě');
                }else if (c=='4'){
                    pinyinToneNumber = pinyinToneNumber.replace('e','è');
                }
            }else {
                if (c == '1') {
                    pinyinToneNumber = pinyinToneNumber.replace('u', 'ū');
                } else if (c == '2') {
                    pinyinToneNumber = pinyinToneNumber.replace('u', 'ú');
                } else if (c == '3') {
                    pinyinToneNumber = pinyinToneNumber.replace('u', 'ǔ');
                } else if (c == '4') {
                    pinyinToneNumber = pinyinToneNumber.replace('u', 'ù');
                }
            }
        }else if ((idx=pinyinToneNumber.indexOf("v"))>=0){
            if (c=='1'){
                pinyinToneNumber = pinyinToneNumber.replace('v','ǖ');
            }else if (c=='2'){
                pinyinToneNumber = pinyinToneNumber.replace('v','ǘ');
            }else if (c=='3'){
                pinyinToneNumber = pinyinToneNumber.replace('v','ǚ');
            }else if (c=='4'){
                pinyinToneNumber = pinyinToneNumber.replace('v','ǜ');
            }
        }else{
            if (pinyinToneNumber.equalsIgnoreCase("ng") || pinyinToneNumber.equalsIgnoreCase("n") ){
                if (c=='2'){
                    pinyinToneNumber = pinyinToneNumber.replace('n','ń');
                }else if (c=='3'){
                    pinyinToneNumber = pinyinToneNumber.replace('n','ň');
                }else if (c=='4'){
                    pinyinToneNumber = pinyinToneNumber.replace('n','ǹ');
                }
            }else if (pinyinToneNumber.equalsIgnoreCase("m")){
                if (c=='2'){
                    pinyinToneNumber = pinyinToneNumber.replace('m','ḿ');
                }
            }
        }
        return pinyinToneNumber;
    }

    private int findYuanYin(String str){
        int idx = -1;
        if ((idx=str.indexOf("a"))>=0||
                (idx=str.indexOf("e"))>=0||
                (idx=str.indexOf("i"))>=0||
                (idx=str.indexOf("o"))>=0||
                (idx=str.indexOf("u"))>=0||
                (idx=str.indexOf("v"))>=0){
            return idx;
        }else{
            return -1;
        }
    }

    private int findTooYuanYin(String str){
        // yue yuan
        int idx = -1;
        if ((idx=str.indexOf("ai"))>=0||
                (idx=str.indexOf("ei"))>=0||
                (idx=str.indexOf("ui"))>=0||
                (idx=str.indexOf("ao"))>=0||
                (idx=str.indexOf("ou"))>=0||
                (idx=str.indexOf("iu"))>=0||
                (idx=str.indexOf("ie"))>=0){
            return idx;
        }else{
            return -1;
        }
    }

    private static char[][] toneDict = {
            {'ü','u','0'},

            {'ā','a','1'},
            {'ē','e','1'},
            {'ī','i','1'},
            {'ō','o','1'},
            {'ū','u','1'},
            {'ǖ','v','1'},

            {'á','a','2'},
            {'é','e','2'},
            {'í','i','2'},
            {'ó','o','2'},
            {'ú','u','2'},
            {'ǘ','v','2'},

            {'ǎ','a','3'},
            {'ě','e','3'},
            {'ǐ','i','3'},
            {'ǒ','o','3'},
            {'ǔ','u','3'},
            {'ǚ','v','3'},

            {'à','a','4'},
            {'è','e','4'},
            {'ì','i','4'},
            {'ò','o','4'},
            {'ù','u','4'},
            {'ǜ','v','4'},

            {'ń','n','2'},
            {'ň','n','3'},
            {'ǹ','n','4'},

            {'ḿ','m','2'}
    };

    private static String[][] measureWords = {
            {"行","háng"},
            {"个","gè"},
            {"山","shān"},
            {"只","zhī"},
            {"打","dá"},
            {"尾","wěi"},
            {"双","shuāng"},
            {"担","dàn"},
            {"壳","ké"},
            {"首","shǒu"},
            {"挑","tiāo"},
            {"令","lìng"},
            {"扎","zhā"},
            {"曲","qǔ"},
            {"单","dān"},
            {"场","chǎng"},
            {"罗","luó"}
    };

    private static char[][] punctuations = {
            {'，', ','},
            {'。', '.'},
            {'！', '!'},
            {'？', '?'},
            {'：', ':'},
            {'“', '"'},
            {'”', '"'},
            {'‘', '\''},
            {'’', '\''}
    };

    private static char[] numeralWords = {'一','二','三','四','五','六','七','八','九','十','百','千','万','亿',
            '壹','贰','叁','肆','伍','陆','柒','捌','玖','拾','佰','仟','萬','几','两'};

}
