JSTools
=======
>- Javascript build tools for java, it is easy to use and has powerful fetures.
>- actully this is a java command line tool.
>- support ANT task.  

Fetures
====
>- merge files and compress
>- jslint
>- jsmeter
>- pre-compile template 
>- support JSP custom tag  

Requirements
===
> Java Runtime 1.5+

설치 & 빌드방법
====
>- build.xml 파일을 열어서 **JSTools workspace** 위치를 자신의 환경에 맞게 수정한다. 
>- 그리고 create_run_jar 태스크를 ANT로 실행한다. 

    <target name="create_run_jar">
    	<property name="src" location="YOUR_JSTOOlS_WORKSPACE"/>
        <jar destfile="${src}/build/JSTools.jar" filesetmanifest="mergewithoutmain">
            <manifest>
                <attribute name="Main-Class" value="com.miconblog.jstools.MainApp"/>
                <attribute name="Class-Path" value="."/>
            </manifest>
        	<fileset dir="${src}/res"/>
            <fileset dir="${src}/bin"/>  	
        	<zipfileset excludes="META-INF/*.SF" src="${src}/lib/yuicompressor-2.4.7.jar"/>
        	<zipfileset excludes="META-INF/*.SF" src="${src}/lib/js2.jar"/>
        </jar>
    </target>

> 그냥 사용하고 싶다면, release 폴더의 jstools.zip 파일을 다운로드 받는다.


실행방법
===
> JSTools.jar 파일을 커맨드라인에서 실행한다. 
> 
    java -jar JSTools.jar [파일목록] [옵션]

> 혹은 ANT 태스크를 작성한다. 자세한 설명은 DOC 파일을 참고한다. 



릴리즈 노트
==============================
ver 2.7.1 since 2013-06-25
----------
- 옵션간의 의존성이 있는 경우 자동으로 해결! (해결 못하는 경우 경로 메시지나 오류 메시지 출력)
- 라이센스 주석(/*!) 추가 

ver 2.7.0 nightly since 2012-08-14
----------
- JSList 최신 버전(2012-08-11) 적용

ver 2.7.0 nightly since 2012-08-03
----------
- 템플릿 머지 기능 추가

ver 2.6.0 since 2012-07-03
----------
- 템플릿 프리 컴파일 기능 추가
 : 옵션은 -tplcomplie
 : 자세한 사용법은 README.txt 파일과 샘플 템플릿(templateSample.tpl)파일 참고한다.

- 라이센스 주석 유지 기능 추가
 : 옵션은 -keeplicense
 : 라이센스 주석은 /*** 으로 시작해야한다.


ver 2.5.0 since 2012-04-09
----------
- jstools 태그 라이브러리 지원 
  : WEB-INF/lib 폴더에 JSTools.jar 파일을 복사해 넣으면 jstools:merge 태그를 사용할수있다. 
- match(/xxx:\/\//) 형태처럼 함수안에 정규 표현식이 있을 경우, 정규식이 주석으로 인식되는 버그 수정 (compress=1 사용할때만 유효)
- ":page/*page" : "page" 형태처럼 따옴표 안에 시작 블록 주석 마커가 포함되어 있는 경우, 블럭주석으로 인식되는 버그 수정 (compress=1 사용할때만 유효)
- JSTools 네임스페이스 변경 --> com.miconblog.jstools 

ver 2.4.1 since 2012-03-05
----------
- 리눅스와 윈도우의 경로 구분자 슬래시와 역슬래시 차이로 인해 유닉스에서 경로 인식 못하는 문제 해결
- 복잡도(jsmeter) 사용시, 스크립트 상에서 오류나면 나머지 복잡도 계산 멈추는 문제 해결, 
  : 스크립트립 오류나면 해당 파일은 건너뜀. 
- 빈파일일 경우 머지 되지 않는 문제 해결 
  : 파일 크기가 3바이트 보다 작으면, 쓸모없는 파일로 판단하고 머지하지 않음! (BOM이 있다면 최소 3byte 이상) 
- 버전 정보 표시 추가
- JSLint 기본 검사항목 수정
  : "Mixed spaces and tabs." - 메시지 그냥 통과
  : "Move the invocation into the parens that contain the function." - 메시지 그냥 통과 

ver 2.4.0 since 2011-11-28
-----
- 치명적인 YUICompressor 라이브러리 오류 수정 
- 주석제거 옵션 버그 수정
  : /** 후덜덜 여기는 제거 안되는 버그 수정 
     * 여기는 주석제거됨.
     */
- JSLint 업그레이드 반영 및 옵션 추가 지정 가능하도록 수정함.
  : 자세한 내용은 jslint.conf 파일을 참고할것 또는 http://www.jslint.com/lint.html 링크 참고
  : 옵션을 지정하려면, -jslint=xxxx 형태로 지정하거나, 옵션파일에서 JSLint '파일명' 으로 지정하면 됨.
- 출력파일에 라인 넘버 표시 옵션 추가 
  : -line-number 옵션을 인라인으로 지정하거나, 옵션파일에서 LineNumber YES로 지정하면됨.
- Java 1.7 지원 
- YUICompressor 업데이트 2.4.6 --> 2.4.7

ver 2.3.0 since 2011-11-11
-----
- UglifyJS 압축 옵션 지원
 : compress=5로 지정하면 UlgifyJS로 압축한다. (현재까지 압축율이 가장 좋다)
- 콘솔에 출력되는 메시지중에 경고나 오류일경우, 색 강조를 위해 오류 출력으로 변경~

ver 2.2.2 since 2011-09-27
---------
- 인코딩 지정방식 변경 
 : EUC-KR로 서비스되는 경우, 소스코드 보통 MS949로 지정되어 있기 때문에 인코딩도 MS949로 지정해야한다.
 : 기존과 같이 소스코드를 UTF-8로 작성하고, 
   EUC-KR서비스로 배포하기 위해 인코딩을 MS949변경 배포하는 경우는 거의 사용하지 않기 때문에..
     소스코드 인코딩과 서비스 캐릭터셋을 통일함. 

ver 2.2.1 since 2011-08-18
---------
- jsmeter 콘솔 출력시, 함수 이름 길이에 맞게 가변적으로 출력하기
- jsmeter 실행시, 머지 결과가 같이 나오지 않도록 수정
- 주석제거 모듈 테스트 코드 정리

ver 2.2.0 since 2011-07-07
---------
- JSLint .jar 파일에 통합  
  : 콘솔 출력 정보 정리
  : JSLint 수행전 BOM 제거후 수행, BOM 이 있을 경우 결함이 발생 하나 사실상 아무런 문제 없음.  
  
- 코드 복잡도 검사를 위한 jsmeter 기능 추가
  : JSLint를 통과하지 못한 코드는 제대로 수행되지 않을수 있다.
  : 참고 URL - http://jsmeter.info, http://code.google.com/p/jsmeter/wiki/CodeMetrics
  : 옵션으로 -jsmeter 사용
  
- 설정파일(-conf)에 BOM이 추가되면 제대로 해석하지 못했던 버그 수정
- 기타 콘솔 출력 정보 정리

ver 2.1.0 since 2011-05-31
---------
- 통계 정보 출력 기능 추가 
  : -infofile 옵션으로 파일로 출력 가능
  : 출력 파일은 확장자에 따라 포맷이 다르다. (ex) xxxx.txt, xxxx.json, xxxx.log
  : log 일 경우엔 단일 정보를 계속해서 누적한다.
  : json 일 경우엔 단일 정보를 JSON 포멧으로 출력한다.
  : txt 일 경우엔 단일 정보를 보기 좋게 단순 출력한다.  

- compress=1 옵션을 이용해 CSS 의 주석을 제거하는 경우, CSS Hack도 같이 지워질 수 있으므로 WARNNING 표시
- 설정파일에 공백이 들어가는 경우에 WARN 메시지 뜨는 오류 수정
- 코드 안에 한글을 사용할 경우 인코딩이 깨지는 문제 해결
- 주석제거 모듈 버그 수정 
  : /* 한줄 블럭 주석안에 http://xxx 링크가 있는 경우 제대로 제거되지 않던 버그 수정됨  */


ver 2.0.1 since 2011-05-26
----------
- RemovePattern 인코딩 방식 변경
  : compress=0 으로 패턴만 지우는 경우에 한글 주석이 깨지는 문제 발견
  : AS IS 기존에는 무조건 UTF-8로 인코딩 하는 것을 / TO BE 설정한 인코딩 방식으로 변경 적용


ver 2.0.0 since 2011-05-12
----------
- JSTools 사용법 변경 (자세한 내용은 위키나 README.txt 문서 참고)
- YUICompress 옵션중에 --line-break, --preserve-semi, --type 옵션 추가
- 원본 파일의 BOM 제거되는 버그 수정: 원본파일은 변경없고, 출력파일의 BOM만 제거
- -conf 옵션 추가 : 기본 설정 지정
- -encoding 옵쳔 추가: 출력 인코딩 지정 (기본은 UTF-8)
- -filetype 옵션 추가 (YUI type 옵션, 기본은 js): css 압축할때 사용함.
- -line-break 옵션 추가 (YUI -libe-break 옵션): 압축파일에 개행이 필요할때 사용함
- -compress 옵션 4 추가 (YUI preser4로 지정하면 불필요한 세미콜론도 날린다.)  
- -pattern 옵션 동작 방식 변경: 해당 정규식 포함된 라인 지우기에서 해당 정규식으로 검색된 문자열만 지우기로 변경
