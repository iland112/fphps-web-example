## Title
Upgrade to Java 21

## Summary
This PR upgrades the project Java runtime to the LTS Java 21 by setting the Gradle Java toolchain to Java 21.

## Changes
- Updated `build.gradle` to set `JavaLanguageVersion.of(21)` in the `java.toolchain` block.

## Why
Upgrading to Java 21 ensures the project runs on the latest LTS JDK with performance, security, and language improvements.

## Validation performed
- Built the project successfully using the Gradle wrapper.
- Ran unit tests: all tests passed.
- CVE scan: no actionable CVEs found.
- Behavior validation: no behavior changes detected.

## Files changed
- `build.gradle`

## Upgrade session
- Upgrade session ID: `20251215060155`
- Plan: `.github/java-upgrade/20251215060155/plan.md`
- Summary: `.github/java-upgrade/20251215060155/summary.md`

## Local verification
Run locally with the Gradle wrapper (Windows PowerShell):
```powershell
.\gradlew clean build
``` 

## Notes and next steps
- Ensure CI and target runtime (staging/production) are configured to use Java 21.
- If you want, I can open the PR page (already available) and paste this description for you, add reviewers, or merge after approvals.

## Suggested reviewers
- @team-lead
- @devops
