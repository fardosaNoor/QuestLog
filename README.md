Project proposal: QuestLog
1. Abstract
QuestLog is a web application designed to help students manage their academic lives through a
gamified, all-in-one productivity platform. Many students struggle to keep track of assignments,
deadlines, and course schedules across multiple platforms, switching between their university
portal, calendar apps, and note-taking tools just to stay organized. QuestLog solves this by
bringing everything into one place while making the experience engaging through game-like
mechanics that reward students for building consistent study habits.
2. The Problem
As students ourselves, we know how easy it is to lose track of things during the semester.
Deadlines are spread out across Canvas, course websites, syllabuses, emails, and sometimes just
what the professor said in lecture. On top of that, students have work, clubs, and their personal
lives. Most of us end up using a mix of a calendar, a notes app, and memory, and something
always slips through. The other half of the problem is motivation. Even when students know
what they must do, it is hard to start on boring tasks, and procrastination builds up until
everything is due in the same week. So, the problem is really two things: students don't have one
place to see everything they need to do, and they don't have much motivation to keep up with it
consistently.
3. How Students Solve This Right Now
Right now, most students use a combination of tools, and none of them fully solve the problem.
Canvas shows assignment due dates, but only for courses that use it, and it doesn't help you plan
when to do the work or include anything outside of school. Google Calendar is good for
scheduling classes and events, but it isn't really built for tracking tasks, and it doesn't care
whether you finish anything. To-do list apps like Todoist, Notion, and MyStudyLife let students
write down tasks and organize them by course, but they are plain and it is easy to stop opening
them after the first few weeks.
4. How QuestLog Makes This Better
QuestLog combines the useful parts of these tools into one app built specifically for students.
Instead of using Canvas for deadlines, Google Calendar for schedules, and a separate app for to-
dos, students can see their courses, tasks, and calendar together. Unlike regular to-do apps,
QuestLog gives students a reason to keep using it with streaks, badges, and a shop, similar to
Duolingo and Habitica. Unlike Habitica, everything is organized around courses and deadlines,
and it connects to the calendar students are already using. The reminder system also helps
students before it's too late, instead of only showing them what they missed.
We think QuestLog is mainly educational, even though it has game parts. The game features are
there to help students build better study habits, manage their time better, and stay on top of their
courses. The entertainment side is just what keeps them motivated to use it.
5. Target Audience
Our target audience is post-secondary students, especially university and college students like us
who are taking several courses at once and have a lot of deadlines to keep track of. High school
students could also use it, but we are mainly designing it around how a university semester
works.
6. Scope and Epics
    Epic 1: Course Management System
The foundation of QuestLog is its Course Management System (CMS), which allows students to
build a personal academic profile. Students manually add their courses, including course name,
code, schedule, and instructor, which then serves as the backbone for every other feature in the
app. Once courses are entered, QuestLog cross-references them with Google Calendar to map
out the student's existing schedule and automatically flag any conflicts or overlaps between
classes and planned activities. For cases where conflicts arise, the CMS includes a priority
system that helps students decide which commitments take precedence. Every other feature in
QuestLog such as assignment tracking, study plan generation, and habit rewards will be pulled
from this course data. Adding courses is already working in our current deployment.
    Epic 2: Profiles and Accounts
This epic covers user accounts and the profile page. It includes registration, login, and profile
creation, along with showing a student's badges, streaks, and level. Like Duolingo, when
students first sign up QuestLog will ask them to set a streak goal, like one week or two weeks,
and then give them a simple plan to reach it. Students will also be able to customize their profile
with the items they unlock from the shop.
    Epic 3: Calendar and Task Management
This epic is the main planner part of the app. Students can manually add tasks with due dates and
link them to courses. QuestLog will work out which tasks are the most urgent based on how
close they are and show those first. This epic is also where we set up the connection to the
Google Calendar API. QuestLog will read the student's existing events from Google Calendar
and show them together with their QuestLog tasks in one calendar view. The course management
system uses this same calendar data for its conflict checking. This epic will also include a study
plan generator that looks at upcoming deadlines and suggests when to start each assignment, so
the work is spread out instead of crammed at the end. If we have time, we would also like to add
a "save the date" feature that pulls events out of emails, but since getting access to Gmail is
difficult, this will be a stretch goal.
    Epic 4: Notifications and Reminders
This epic makes sure students actually hear about their deadlines. QuestLog will send in-app and
email notifications, and students will be able to choose how early and how often they want to be
reminded. Each morning students can get a summary of their tasks, classes, and deadlines for the
day. Reminders will get more frequent as an urgent deadline gets closer, and if a student hasn't
finished a task that day, they will get a streak-saver reminder. Students can also set quiet hours
so they aren't getting notifications at 2am. We plan to use the Resend API to send the emails.
    Epic 5: Shop and Rewards System
This epic is what makes QuestLog feel like a game. Students earn XP and coins by finishing
tasks and keeping streaks. Coins can be spent in the shop on things like themes, avatars, profile
frames, and icons. Students can also buy streak freezes, which protect their streak on a busy day.
We also want to add limited-time seasonal items, like exam season or holiday themes, to keep
things interesting throughout the year.
7. APIs
QuestLog will use two web REST APIs. The main one is the Google Calendar API, which we
will call over HTTPS to read a student's calendar events after they give permission through
Google sign-in. This data is used in the Calendar and Task Management epic to show events
alongside tasks, and in the Course Management System to detect overlaps between classes and
other commitments. The second is the Resend API, which we will call over HTTPS to send
reminder emails and daily summaries for the Notifications epic.
8. Is This Enough Work for Five People?
We think it is. Each member owns one epic, and each epic has several smaller features inside it,
so everyone has a major feature to work on. The epics also depend on each other, since rewards
need tasks to be completed and notifications need deadlines from the calendar, so we will need to
work together and agree on how the parts connect.