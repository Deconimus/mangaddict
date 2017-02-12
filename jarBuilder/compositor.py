import os
import shutil
import json
import hashlib
import zipfile

path = os.path.dirname(os.path.abspath(__file__)).replace("\\", "/")

def build(projectPath, data, jarBuilder):
	
	jarName = data["jarName"] if "jarName" in data else "app"
	outDir = data["outDir"] if "outDir" in data else ""
	mainClass = data["main"] if "main" in data else ""
	srcDirs = data["sourceDirs"] if "sourceDirs" in data else None
	imports = data["imports"] if "imports" in data else []
	dynImports = data["dynImports"] if "dynImports" in data else []
	dynImportsExt = data["dynImportsExt"] if "dynImportsExt" in data else []
	extLibDir = data["extLibDir"] if "extLibDir" in data else "lib"
	packFiles = data["packFiles"] if "packFiles" in data else []
	
	if srcDirs is None or len(srcDirs) <= 0:
		srcDirs = [projectPath+"/src"]
		
	if not jarName.lower().endswith(".jar"):
		jarName = jarName+".jar"
	
	completePaths(srcDirs, projectPath)
	completePaths(imports, projectPath)
	completePaths(dynImports, projectPath)
	completePaths(dynImportsExt, projectPath)
	extLibDir = completePath(extLibDir, projectPath)
	completePaths(packFiles, projectPath)
	outDir = completePath(outDir, projectPath)
	
	
	checkForProjects(imports, dynImports, dynImportsExt, srcDirs, jarBuilder)
	checkForProjectsDyn(dynImports, jarBuilder)
	checkForProjectsDyn(dynImportsExt, jarBuilder)
	
	
	print("Building "+jarName)
	
	jarBuilder.build(projectPath, jarName, outDir, mainClass, srcDirs, imports,
					 dynImports, dynImportsExt, extLibDir, packFiles)
	
	
	jarShrink = None
	jarShrinkKeep = []
	
	if "jarShrink" in data and "path" in data["jarShrink"]:
		
		jarShrink = data["jarShrink"]["path"]
		
		if not os.path.exists(jarShrink):
			print("\""+jarShrink+"\" not found.")
			jarShrink = None
		
		if "keep" in data["jarShrink"]:
			
			jarShrinkKeep = data["jarShrink"]["keep"]
		
			
	if not jarShrink is None:
		
		jp = "\""+outDir+"/"+jarName+"\""
		
		ks = ""
		for k in jarShrinkKeep:
			ks = ks+" -k \""+k+"\""
			
		jarShrinkTmp = path+"/jarShrink_tmp"
		
		print("Shrinking "+jarName)
		
		os.system("java -jar \""+jarShrink+"\" "+jp+" -out "+jp+" -t \""+jarShrinkTmp+"\" -n "+ks)
		
		shutil.rmtree(jarShrinkTmp)
		
	print(jarName+" done.")
		

def completePaths(paths, projectPath):
	
	for i in range(0, len(paths)):
		
		paths[i] = completePath(paths[i], projectPath)
			
			
def completePath(p, projectPath):
	
	p = p.replace("\\", "/")
	
	if not p.startswith("/") and not (len(p) > 1 and p[1] == ":"):
		
		return projectPath+"/"+p
		
	return p
	
	
def isProject(directory):
	return os.path.exists(directory+"/build.json") or directory.endswith("/build.json")
	

def checkForProjectsDyn(imports, jarBuilder):
	
	checkForProjects(imports, None, None, None, jarBuilder, dynamic=True)


def checkForProjects(imports, dynImports, dynImportsExt, srcDirs, jarBuilder, dynamic=False):
	
	i = 0
	while i < len(imports):
		
		project = imports[i]
		
		if isProject(imports[i]):
			
			if project.endswith("/build.json"):
				project = project[:project.rindex("/")]
			
			data = getBuildData(project)
			
			if not data is None:
				
				if dynamic:
					
					imports[i] = buildDependency(project, data, jarBuilder)
					
				else:
					
					if not "srcDirs" in data:
						data["srcDirs"] = [project+"/src"]
					
					complete = lambda p: completePath(p, project)
						
					appendElementsFromMap(data, imports, "imports", proc=complete)
					appendElementsFromMap(data, dynImports, "dynImports", proc=complete)
					appendElementsFromMap(data, dynImportsExt, "dynImportsExt", proc=complete)
					appendElementsFromMap(data, srcDirs, "srcDirs", proc=complete)
					
					imports[i] = None
					
			else:
				
				imports[i] = None
		
		if imports[i] is None or not os.path.exists(imports[i]):
			
			del imports[i]
			i = i-1
			
		i = i+1
	
	
def getBuildData(project):
	
	with open(project+"/build.json") as f:
		data = json.load(f)
		
	if data is None or len(data) <= 0:
		print("\""+project+"/build.json\" is corrupted.")
		return None
		
	return data
	
	
def buildDependency(project, data, jarBuilder):
	
	data["outDir"] = path+"/tmp_libs"
	
	if not "jarName" in data:
		data["jarName"] = project.replace("\\", "/").split("/")[-1]
		
	build(project, data, jarBuilder)
	
	libPath = path+"/tmp_libs"+data["jarName"]
	
	if not libPath.lower().endswith(".jar"):
		libPath = libPath+".jar"
		
	if not os.path.exists(libPath):
		
		return None
	
	return libPath
	
	
def appendElementsFromMap(m, l, key, proc=None):
	
	if key in m:
		
		for e in m[key]:
			
			if not proc is None and not e is None:
				e = proc(e)
				
			if not e in l:
				l.append(e)

				
def cleanup():
	
	removeTmpLibs()

				
def removeTmpLibs():
	
	if os.path.exists(path+"/tmp_libs"):
		
		shutil.rmtree(path+"/tmp_libs")
		
		