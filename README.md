# Baul plugin

Baul is a crate-like plugin for Minecraft servers.
Each item you can get from a baul has a rarity, this
rarity determines the odds of you winning the item.

You can choose from a large selection of animations
for the opening of your baul!

## For developers
#### How to start contributing?
###### 1. Fork the repository
- Click "Fork" in the top-right corner on GitHub
- Select your account to create a copy
###### 2. Clone Your Fork
```bash
git clone https://github.com/BarrioUniversitario/Baul.git
cd ./Baul
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
