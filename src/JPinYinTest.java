import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;


public class JPinYinTest {
    public static JPinYin jPinYin = null;

    public static void main(String[] args) {
        jPinYin = new JPinYin("data");

        System.out.println(jPinYin.convert("嗯嗯",JPinYin.TONE_NONE,"-"));
        System.out.println(jPinYin.convert("一行人",JPinYin.TONE_UNICODE,"="));
        System.out.println(jPinYin.convertLink("两只黄鹂鸣翠柳，一行白鹭上青天！"));

        testFormatTone();
        testText();

        testLiangCi();

        testFormatToUnicodeToneAll();

        System.out.println("*************************");
    }

    public static void testFormatToUnicodeToneAll(){

        int count = 0;

        Set e = jPinYin.dictWord.entrySet();
        Iterator<Map.Entry<Character,Set<String>>> itr = e.iterator();

        System.out.println("testFormatToUnicodeToneAll-begin");
        while(itr.hasNext()){
            Map.Entry<Character,Set<String>> x = itr.next();
            Set<String> s = x.getValue();
            Iterator<String> sitr = s.iterator();
            while(sitr.hasNext()){
                String pinyin = sitr.next();
                testFormatToUnicodeTone(jPinYin,"["+(++count)+"] ",jPinYin.formatTone(pinyin,JPinYin.TONE_NUMBER),pinyin);
            }

        }
        System.out.println("testFormatToUnicodeToneAll-finished");
    }

    public static void testFormatToUnicodeTone(JPinYin jPinYin, String num, String str, String src){
        assertEquals(src,jPinYin.formatToUnicodeTone(str));
    }


    public static void testLiangCi(){
        System.out.println(jPinYin.convertSentence("两只黄鹂鸣翠柳，一行白鹭上青天！",JPinYin.TONE_UNICODE));


        System.out.println(jPinYin.convertSentence("两只黄鹂鸣翠柳，一行白鹭上青天！",JPinYin.TONE_NUMBER));
        System.out.println(jPinYin.convertSentence("两只黄鹂鸣翠柳，一行白鹭上青天！",JPinYin.TONE_NUMBER));
        System.out.println(jPinYin.convert("两只黄鹂鸣翠柳，一行白鹭上青天！",JPinYin.TONE_NUMBER,"-",true,true));

        System.out.println(jPinYin.convertSentence("徐霞客临终前，所说的那句话：  “汉代的张骞，唐代的玄奘，元代的耶律楚材，" +
                "他们都曾游历天下，然而，他们都是接受了皇帝的命令，受命前往四方。”  “我只是个平民，没有受命，只是穿着布衣，" +
                "拿着拐杖，穿着草鞋，凭借自己，游历天下，故虽死，无憾。”  所谓千秋霸业，万古流芳，以及一切的一切，只是粪土。" +
                "先变成粪，再变成土。  伟大就是——按照自己的方式，去度过人生。  明月哥\n" +
                "\n" +
                "作者：徐帅\n" +
                "链接：https://www.zhihu.com/question/20531788/answer/15397210\n" +
                "来源：知乎\n" +
                "著作权归作者所有。商业转载请联系作者获得授权，非商业转载请注明出处。",JPinYin.TONE_UNICODE));
    }

    public static void testText(){

        assertEquals("convertSentence",jPinYin.convertSentence("convertSentence",JPinYin.TONE_NUMBER));

        assertEquals("'con'vert,?:!\"\"",jPinYin.convertSentence("‘con’vert，？：！“”",JPinYin.TONE_NUMBER));

        assertEquals("chang2cheng2",jPinYin.convertSentence("长城",JPinYin.TONE_NUMBER));

        assertEquals("liang3zhi1huang2li2ming2cui4liu3,yi1hang2bai2lu4shang4qing1tian1!",jPinYin.convertSentence("两只黄鹂鸣翠柳，一行白鹭上青天！",JPinYin.TONE_NUMBER));

        assertEquals("Shan4Tian2Fang1",jPinYin.convertName("单田芳",JPinYin.TONE_NUMBER));

        assertEquals(new String[]{"dan1","shan4","chan2"},jPinYin.convertChar('单',JPinYin.TONE_NUMBER));
    }

    public static void testFormatTone(){

        assertEquals("wo",jPinYin.formatTone("wǒ",JPinYin.TONE_NONE));
        assertEquals("wo3",jPinYin.formatTone("wǒ",JPinYin.TONE_NUMBER));

        assertEquals("yue wo",jPinYin.formatTone("yüè wǒ",JPinYin.TONE_NONE));
        assertEquals("yue4 wo3",jPinYin.formatTone("yüè wǒ",JPinYin.TONE_NUMBER));

        assertEquals("ya'an",jPinYin.formatTone("yǎ'ān",JPinYin.TONE_NONE));
        assertEquals("ya3'an1",jPinYin.formatTone("yǎ'ān",JPinYin.TONE_NUMBER));

        assertEquals("ya an",jPinYin.formatTone("yǎ ān",JPinYin.TONE_NONE));
        assertEquals("ya3 an1",jPinYin.formatTone("yǎ ān",JPinYin.TONE_NUMBER));

        assertEquals("ya\tan",jPinYin.formatTone("yǎ\tān",JPinYin.TONE_NONE));
        assertEquals("ya3\tan1",jPinYin.formatTone("yǎ\tān",JPinYin.TONE_NUMBER));

        assertEquals("wai",jPinYin.formatTone("wǎi",JPinYin.TONE_NONE));
        assertEquals("wai3",jPinYin.formatTone("wǎi",JPinYin.TONE_NUMBER));

        assertEquals("chang\tcheng",jPinYin.formatTone("cháng\tchéng",JPinYin.TONE_NONE));
        assertEquals("chang2\tcheng2",jPinYin.formatTone("cháng\tchéng",JPinYin.TONE_NUMBER));
    }


}
