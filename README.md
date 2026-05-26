# ACubelets plugin

ACubelets are a crate-like plugin.
Each item you can get from a cubelet has a rarity, this
rarity determines the odds of you wining the item.

You can choose from a large selection of animations 
for the opening of your cubelet!

> Available for purchase at:
[SpigotMC](https://www.spigotmc.org/resources/acubelets-%E2%AD%90-23-animations-%E2%AD%90-1-16-1-21-8.78092/) and
[BuiltByBit](https://builtbybit.com/resources/acubelets.45566/).

## For developers
#### How to start contributing?
###### 1. Fork the repository
- Click "Fork" in the top-right corner on GitHub
- Select your account to create a copy
###### 2. Clone Your Fork
```bash
git clone https://github.com/your_username/ACubelets.git
cd ./ACubelets
```
###### 3. Create a new branch
```bash
git branch <your_branch_name>
git switch your_branch_name
```
###### 4. Make some changes in source
###### 5. Please, test your changes!
###### 6. Commit, pull, merge and push your changes in forked repository
```bash
git add .
git commit -m "feat: add new animation type"

git switch master
git pull origin master

git merge your_branch_name
git push origin master
```
**Note:** We prefer using conventional commits to keep the history clear and maintainable.
Examples of conventional commit prefixes: feat, fix, chore, docs, refactor, test.
###### 6. Open a pull request by clicking the "Contribute" button on your fork's GitHub page
###### Thanks for supporting ❤️

#### Compiling
###### Use gradle to compile a jar
> Linux / MacOS
```bash
cd <project_root_directory>
chmod +x ./gradlew
./gradlew build
```
> Windows
```powershell
cd <project_root_directory>
.\gradlew.bat build
```
Find the compiled jar in `build/libs` directory

[![Ask DeepWiki](https://deepwiki.com/badge.svg)](https://deepwiki.com/IDavidML/ACubelets)
