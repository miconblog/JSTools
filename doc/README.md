# JSTools 초간단 사용법
* JSTools는 Java로 작성된 JavaScript 파일 배포를 위한 지원 도구 입니다.
* 파일 병합, 압축, 난독화, 템플릿 컴파일, 정적분석, 코드 복잡도 계산등을 할수있습니다.

## 기본 문법
    %>java -jar JSTools.jar <파일목록> [-옵션]

### <파일목록>
 - 필수 옵션으로 반드시 파일명을 나열해야한다. 
 - -conf 옵션 보다 우선한다. 
 
> 사용법 
> 
      1. 파일을 공백으로 나열한다. ex) file1 file2 file3
      2. 폴더를 나열하면 폴더 하위에 모든 js 파일을 포함한다. ex) file1 dir1 dir2
      3. 모든 경로는 상대경로를 지원한다. ex) ../dir1/file1 ./file2
            
### [옵션]   
      1. 모든 옵션은 마이너스(-)로 시작한다.
      2. 모든 옵션을 한꺼번에 지정할수있다.
    
#### -compress=[0~5] : 기본값 0
      0 일 경우, (기본값) 파일을 머지한다.
      1 일 경우, 파일 머지 + 주석 제거 
      2 일 경우, 파일 머지 + 압축(YUI)
      3 일 경우, 파일 머지 + 압축(YUI) + 난독화(obfuscate)
      4 일 경우, 파일 머지 + 압축(YUI) + 난독화(obfuscate) + 불필요한 세미콜론제거
      5 일 경우, UglifyJS로 압축한다. 압축율이 가장좋다.
           ex) java -jar JSTools.jar file1 file2 dir1 output=./result.js
           ex) java -jar JSTools.jar file1 file2 dir1 -compress=1 
           ex) java -jar JSTools.jar file1 file2 dir1 -compress=2
           ex) java -jar JSTools.jar file1 file2 dir1 -compress=3
           ex) java -jar JSTools.jar file1 file2 dir1 -compress=4
           ex) java -jar JSTools.jar file1 file2 dir1 -compress=5
         
#### -jslint[=옵션파일지정]

    옵션파일을 지정해, jslint를 설정한다. 자세한 내용은 첨부된 jslint.conf 파일 참고한다.   
        ex) java -jar JSTools.jar file1 file2 dir1 -jslint
        ex) java -jar JSTools.jar file1 file2 dir1 -jslint=./jslint.conf
      
#### -jsmeter : 코드 정적분석(JSMeter)을 실행한다.
    반드시 /jslint 폴더는 JSTools.jar 파일과 동일한 위치에 있어야한다.  
        ex) java -jar JSTools.jar file1 file2 dir1 -jsmeter
      
#### -list=xxxx
    별도의 목록 파일을 지정할수있다. 샘플 목록 파일(mergelist.txt)를 참고한다.
        ex) java -jar JSTools.jar -list=./mergelist.txt
           
#### -conf=xxxx
    별도의 설정 파일을 지정할수있다. 샘플 설정 파일(jstools.conf)을 참고한다.
        ex) java -jar JSTools.jar -conf=./jstools.conf
 
#### -pattern=/xxxx/
  	xxxx 형태의 정규식과 매칭되는 문자열을 지운다.
  	디버깅을 위한 console.log 를 지우는 정규식
  	    ex) java -jar JSTools.jar file1 file2 dir1 -pattern=/console.log\([^;]*\);?/ 
  	    
#### -output=xxxx
  	출력할 파일을 지정한다. 지정하지 않으면 화면에 출력된다.
  	도스 커멘드 상에서 실행할경우 파이프 라인(>)을 이용해도 된다.
  	       ex) java -jar JSTools.jar file1 file2 dir1 -output=result.js
  	       ex) java -jar JSTools.jar file1 file2 dir1 > result.js 
  	       
#### -encoding=xxx
  	입출력 파일 인코딩을 지정한다. 기본값은 UTF-8 이다. 만약 JS 소스코드가 EUC-KR로 인코딩 되었다면, 반드시 MS949로 지정해야한다. 
  	     
#### -line-break=xxx
  	압축 옵션 2,3번을 사용했을경우, 한줄로 압축되는 녀석들을 지정한 문자열 갯수에 따라 줄바꿈 해준다.
  	       ex) java -jar JSTools.jar file1 file2 dir1 -compress=2 -line-break=120
  	  
#### -filetype=[js|css]
  	머지할 파일 타입을 결정한다. 기본값은 js 지만, css 파일을 압축하고 싶다면 css로 지정한다.
  	       ex) java -jar JSTools.jar cssfile1 cssfile2 dir1 -compress=3 -fileType=css
 
#### -infofile=xxx.[txt|json]
    압축 전후의 통계 정보를 파일로 출력한다. 확장자에 따라 출력 포맷이 달라진다.  
       
#### -line-number
    출력 파일에 코드 라인 정보를 추가한다.  
             
####-tplComplie[=TemplateNameSpace]
    템플릿을 컴파일 한다.
             
#### -keeplicense
    파일 압축시 라이센스 주석을 지우지 않고 유지한다. 단, 라이센스 주석은 /*** 으로 시작해야한다.
             
## 템플릿 문법

### 변수 치환 
  {%= 변수 %}
  
### 자바스크립트 문법 사용 
  {% 자바스크립트 표현식 %}
   
### 템플릿 ID 지정 (단, ID 사이에 공백 있으면 안됨)
>     <!--[템플릿_ID] -->
>     	 템플릿 코드 
>     <!--[/템플릿_ID] -->

### 템플릿 예제
**1 템플릿 파일을 만든다.**
>     <!--[sample_tpl_id]-->
> 	  {% 
> 		for(var i=0; i<data.length; ++i){
> 			var item = data[i]; 
> 	  %}
> 		{%if( item.header ) { %}
> 		<h2 class='tb-header{% if(i==0){ %} top{%}else{%} node{%}%}'>{%=item.header%}</h2>
> 		{%}%}
> 	
> 		<div class='tb-item{%if(item.isFirst){%} first{%}%}{%if(item.isLast){%} last{%}%} _tb_select({%=i%})'>
> 			{%=item.title%}
> 			{% if(item.hasChild) { %}
> 				<span class='tb-child'></span>
> 			{%}%}
> 		</div>
> 
> 	{% } %}
> 	<!--[/sample_tpl_id]-->
   
**2. 템플릿을 컴파일해서 생성된 js 파일을 삽입한다.**
   
**3. 아래와 같이 호출해 사용한다.** 
>     var sTpl = Template.get('sample_tpl_id', {
> 		data : [
> 			{title:'제목1', header:'test', isFirst:true},
> 			{title:'제목2', hasChild:true},
> 			{title:'제목3' isLast:true}]		
> 	});
> 	wel.html(sTpl);
   
## ANT 빌드 적용 예제
	<target name="merge_js_files" description="merge js files from file list.">
		<echo>Merge Files...</echo>
		<exec executable="java">
			<arg line="-jar" />
			<arg path="${tools.dir}/JSTools.jar" />
			<arg value="-conf=${source.dir}/jstools.conf"/>
			<arg value="-compress=2"/>
			<arg value="-output=${deploy.dir}/result.js"/>
		</exec>
	</target>
	
	
## Taglib 적용 예제
1) JSP 파일 상단에 태그 라이브러리를 사용하기 위해 아래와 같이 기술한다. 
>     <%@ taglib uri="http://miconblog.com/jstools" prefix="jstools" %>

2) 같은 JSP 파일 안에 삽입할 스크립트 태그를 아래와 같이 작성한다.
>     <jstools:merge mergedFile="/js/release/app.js" debug="N">
>     /js/lib/collie.min.js
>     /js/lib/FPSConsole.js
>     /js/lib/AnimateJS.js
>     /js/common/common.debug.js
>     /js/app/Message.js
>     /js/app/Configure.js
>     /js/app/UI.Background.js
>     /js/app/Object.Basket.js
>     /js/app/Object.BallPool.js
>     /js/app/Controller.js
>     /js/app/Game.js
>     </jstools:merge>
  - mergedFile 속성은 기술된 파일이 하나로 압축된 파일의 위치를 나타낸다. 
        압축된 해당 파일이 없다면, 기술된 파일을 하나씩 삽입한다.
  - debug 속성이 Y면, 기술된 파일을 하나씩 삽입한다.

3) 목록 파일을 이용해 기술하려면 아래와 같이 작성한다. 
>     <jstools:merge mergedFile="/js/release/app.js" mergeFileList="builder/mergelist.txt" debug="Y">
>     </jstools:merge>
  - mergeFileList 속성을 사용하려면, 반드시 mergelist.txt 파일 안에 절대 경로의 @ROOT 값과 상대경로로 치환할 @REPLACEMENT 값을 지정해야한다. 
  - 만약 해당 값이 없다면 mergeFileList는 동작하지 않는다.
  


### 태그 라이브러리를 JSP에서 인식하는 방법
  - 자동으로 태그라이브러리 인식하기
    - 서블릿 2.4 이상부터는 web.xml 파일에 tld 설정을 따로 지정하지 않아도 WEB-INF 하위에 tld 파일이 있다면 자동 인식된다.
    - jstools tld 파일은 JSTools.jar 파일의 META-INF/tags 폴더에 있으므로,  
    - WEB-INF 하위에 lib 폴더를 만들고, JSTools.jar 파일을 복사해 넣으면 자동으로 인식된다.    
  
## FAQ 
#### Ant 실행시 이클립스 Console 창에서 한글이 깨질 경우 
    antbuild.xml 파일에서 우클릭 > Run As > External Tools Configurations... 선택 
    JRE 탭 > Runtime JRE: > Run in the same JRE as workspace 으로 설정하면 제대로 나옵니다. 

#### 그냥 이클립스 Console 창에서 한글이 깨지는 경우 
    Run Configuration > 작성중인 Application 선택 
    Common 탭 > Console Encoding > Other 에서  EUC-KR 선택 (없으면 직접 지정)

#### 메이븐 Repo에 jstools 배포 방법
> mvn deploy:deploy-file -Dfile=build\jstools-2.7.1.jar -Durl=[Respo URL] -DgroupId=com.miconblog -DartifactId=jstools -Dversion=2.7.1