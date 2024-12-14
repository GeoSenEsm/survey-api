SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
/****** Object:  Table [dbo].[identity_user]    Script Date: 14.12.2024 22:42:21 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[identity_user](
	[id] [uniqueidentifier] NOT NULL,
	[username] [nvarchar](255) NOT NULL,
	[password_hash] [nvarchar](255) NOT NULL,
	[role] [nvarchar](100) NOT NULL,
PRIMARY KEY CLUSTERED
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY],
UNIQUE NONCLUSTERED
(
	[username] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[initial_survey]    Script Date: 14.12.2024 22:42:21 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[initial_survey](
	[id] [uniqueidentifier] NOT NULL,
	[row_version] [timestamp] NOT NULL,
	[state] [int] NOT NULL,
PRIMARY KEY CLUSTERED
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[initial_survey_option]    Script Date: 14.12.2024 22:42:21 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[initial_survey_option](
	[id] [uniqueidentifier] NOT NULL,
	[question_id] [uniqueidentifier] NULL,
	[order] [int] NOT NULL,
	[content] [nvarchar](250) NOT NULL,
	[row_version] [timestamp] NOT NULL,
PRIMARY KEY CLUSTERED
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[initial_survey_question]    Script Date: 14.12.2024 22:42:21 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[initial_survey_question](
	[id] [uniqueidentifier] NOT NULL,
	[survey_id] [uniqueidentifier] NULL,
	[order] [int] NOT NULL,
	[content] [nvarchar](250) NOT NULL,
	[row_version] [timestamp] NOT NULL,
PRIMARY KEY CLUSTERED
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[localization_data]    Script Date: 14.12.2024 22:42:21 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[localization_data](
	[id] [uniqueidentifier] NOT NULL,
	[respondent_id] [uniqueidentifier] NOT NULL,
	[participation_id] [uniqueidentifier] NULL,
	[date_time] [datetimeoffset](0) NOT NULL,
	[latitude] [decimal](8, 6) NOT NULL,
	[longitude] [decimal](9, 6) NOT NULL,
	[row_version] [timestamp] NOT NULL,
PRIMARY KEY CLUSTERED
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY],
UNIQUE NONCLUSTERED
(
	[respondent_id] ASC,
	[date_time] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[number_range]    Script Date: 14.12.2024 22:42:21 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[number_range](
	[id] [uniqueidentifier] NOT NULL,
	[from] [int] NOT NULL,
	[to] [int] NOT NULL,
	[from_label] [nvarchar](50) NULL,
	[to_label] [nvarchar](50) NULL,
	[question_id] [uniqueidentifier] NOT NULL,
	[row_version] [timestamp] NOT NULL,
PRIMARY KEY CLUSTERED
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[option]    Script Date: 14.12.2024 22:42:21 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[option](
	[id] [uniqueidentifier] NOT NULL,
	[order] [int] NOT NULL,
	[question_id] [uniqueidentifier] NULL,
	[label] [nvarchar](50) NULL,
	[row_version] [timestamp] NOT NULL,
	[show_section] [int] NULL,
	[image_path] [nvarchar](250) NULL,
PRIMARY KEY CLUSTERED
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY],
UNIQUE NONCLUSTERED
(
	[order] ASC,
	[question_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY],
UNIQUE NONCLUSTERED
(
	[label] ASC,
	[question_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[option_selection]    Script Date: 14.12.2024 22:42:21 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[option_selection](
	[id] [uniqueidentifier] NOT NULL,
	[question_answer_id] [uniqueidentifier] NOT NULL,
	[option_id] [uniqueidentifier] NULL,
	[row_version] [timestamp] NOT NULL,
PRIMARY KEY CLUSTERED
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[question]    Script Date: 14.12.2024 22:42:21 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[question](
	[id] [uniqueidentifier] NOT NULL,
	[order] [int] NOT NULL,
	[section_id] [uniqueidentifier] NULL,
	[content] [nvarchar](250) NOT NULL,
	[question_type] [int] NOT NULL,
	[required] [bit] NOT NULL,
	[row_version] [timestamp] NOT NULL,
PRIMARY KEY CLUSTERED
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY],
UNIQUE NONCLUSTERED
(
	[order] ASC,
	[section_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[question_answer]    Script Date: 14.12.2024 22:42:21 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[question_answer](
	[id] [uniqueidentifier] NOT NULL,
	[participation_id] [uniqueidentifier] NOT NULL,
	[question_id] [uniqueidentifier] NOT NULL,
	[numeric_answer] [int] NULL,
	[row_version] [timestamp] NOT NULL,
	[yes_no_answer] [bit] NULL,
PRIMARY KEY CLUSTERED
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY],
UNIQUE NONCLUSTERED
(
	[participation_id] ASC,
	[question_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[research_area]    Script Date: 14.12.2024 22:42:21 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[research_area](
	[id] [uniqueidentifier] NOT NULL,
	[latitude] [decimal](8, 6) NOT NULL,
	[longitude] [decimal](9, 6) NOT NULL,
	[row_version] [timestamp] NOT NULL,
	[order] [int] NULL,
PRIMARY KEY CLUSTERED
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[respondent_data]    Script Date: 14.12.2024 22:42:21 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[respondent_data](
	[id] [uniqueidentifier] NOT NULL,
	[identity_user_id] [uniqueidentifier] NULL,
	[survey_id] [uniqueidentifier] NOT NULL,
	[row_version] [timestamp] NOT NULL,
PRIMARY KEY CLUSTERED
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY],
UNIQUE NONCLUSTERED
(
	[identity_user_id] ASC,
	[survey_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[respondent_data_option]    Script Date: 14.12.2024 22:42:21 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[respondent_data_option](
	[id] [uniqueidentifier] NOT NULL,
	[respondent_data_question_id] [uniqueidentifier] NOT NULL,
	[option_id] [uniqueidentifier] NULL,
	[row_version] [timestamp] NOT NULL,
PRIMARY KEY CLUSTERED
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[respondent_data_question]    Script Date: 14.12.2024 22:42:21 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[respondent_data_question](
	[id] [uniqueidentifier] NOT NULL,
	[respondent_id] [uniqueidentifier] NOT NULL,
	[question_id] [uniqueidentifier] NOT NULL,
	[row_version] [timestamp] NOT NULL,
PRIMARY KEY CLUSTERED
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY],
UNIQUE NONCLUSTERED
(
	[respondent_id] ASC,
	[question_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[respondent_to_group]    Script Date: 14.12.2024 22:42:21 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[respondent_to_group](
	[id] [uniqueidentifier] NOT NULL,
	[respondent_id] [uniqueidentifier] NULL,
	[group_id] [uniqueidentifier] NULL,
	[row_version] [timestamp] NOT NULL,
PRIMARY KEY CLUSTERED
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[respondents_group]    Script Date: 14.12.2024 22:42:21 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[respondents_group](
	[id] [uniqueidentifier] NOT NULL,
	[row_version] [timestamp] NOT NULL,
	[name] [nvarchar](250) NULL,
PRIMARY KEY CLUSTERED
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
-- Insert the group named "All" if it doesn't exist
IF NOT EXISTS (SELECT 1 FROM [dbo].[respondents_group] WHERE name = 'All')
BEGIN
    INSERT INTO [dbo].[respondents_group] (id, name)
    VALUES (NEWID(), 'All');
END
GO
/****** Object:  Table [dbo].[section_to_user_group]    Script Date: 14.12.2024 22:42:21 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[section_to_user_group](
	[id] [uniqueidentifier] NOT NULL,
	[section_id] [uniqueidentifier] NOT NULL,
	[group_id] [uniqueidentifier] NOT NULL,
	[row_version] [timestamp] NOT NULL,
PRIMARY KEY CLUSTERED
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[sensor_data]    Script Date: 14.12.2024 22:42:21 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[sensor_data](
	[id] [uniqueidentifier] NOT NULL,
	[respondent_id] [uniqueidentifier] NOT NULL,
	[date_time] [datetimeoffset](0) NOT NULL,
	[temperature] [decimal](4, 2) NOT NULL,
	[humidity] [decimal](5, 2) NULL,
PRIMARY KEY CLUSTERED
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY],
UNIQUE NONCLUSTERED
(
	[respondent_id] ASC,
	[date_time] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[survey]    Script Date: 14.12.2024 22:42:21 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[survey](
	[id] [uniqueidentifier] NOT NULL,
	[name] [nvarchar](100) NOT NULL,
	[row_version] [timestamp] NOT NULL,
	[state] [int] NOT NULL,
	[creation_date] [datetimeoffset](0) NOT NULL,
PRIMARY KEY CLUSTERED
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY],
 CONSTRAINT [unique_survey_name] UNIQUE NONCLUSTERED
(
	[name] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[survey_participation]    Script Date: 14.12.2024 22:42:21 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[survey_participation](
	[id] [uniqueidentifier] NOT NULL,
	[respondent_id] [uniqueidentifier] NULL,
	[survey_id] [uniqueidentifier] NOT NULL,
	[date] [datetimeoffset](0) NOT NULL,
	[row_version] [timestamp] NOT NULL,
PRIMARY KEY CLUSTERED
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY],
UNIQUE NONCLUSTERED
(
	[respondent_id] ASC,
	[date] ASC,
	[survey_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[survey_participation_time_slot]    Script Date: 14.12.2024 22:42:21 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[survey_participation_time_slot](
	[id] [uniqueidentifier] NOT NULL,
	[start] [datetimeoffset](0) NOT NULL,
	[finish] [datetimeoffset](0) NOT NULL,
	[survey_sending_policy_id] [uniqueidentifier] NULL,
	[row_version] [timestamp] NOT NULL,
	[is_deleted] [bit] NOT NULL,
PRIMARY KEY CLUSTERED
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[survey_section]    Script Date: 14.12.2024 22:42:21 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[survey_section](
	[id] [uniqueidentifier] NOT NULL,
	[order] [int] NOT NULL,
	[name] [nvarchar](100) NULL,
	[survey_id] [uniqueidentifier] NULL,
	[visibility] [int] NOT NULL,
	[row_version] [timestamp] NOT NULL,
	[display_on_one_screen] [bit] NOT NULL,
PRIMARY KEY CLUSTERED
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY],
UNIQUE NONCLUSTERED
(
	[order] ASC,
	[survey_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[survey_sending_policy]    Script Date: 14.12.2024 22:42:21 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[survey_sending_policy](
	[id] [uniqueidentifier] NOT NULL,
	[survey_id] [uniqueidentifier] NULL,
	[row_version] [timestamp] NOT NULL,
PRIMARY KEY CLUSTERED
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
ALTER TABLE [dbo].[identity_user] ADD  DEFAULT (newid()) FOR [id]
GO
ALTER TABLE [dbo].[initial_survey] ADD  DEFAULT (newid()) FOR [id]
GO
ALTER TABLE [dbo].[initial_survey_option] ADD  DEFAULT (newid()) FOR [id]
GO
ALTER TABLE [dbo].[initial_survey_question] ADD  DEFAULT (newid()) FOR [id]
GO
ALTER TABLE [dbo].[localization_data] ADD  DEFAULT (newid()) FOR [id]
GO
ALTER TABLE [dbo].[number_range] ADD  DEFAULT (newid()) FOR [id]
GO
ALTER TABLE [dbo].[option] ADD  DEFAULT (newid()) FOR [id]
GO
ALTER TABLE [dbo].[option_selection] ADD  DEFAULT (newid()) FOR [id]
GO
ALTER TABLE [dbo].[question] ADD  DEFAULT (newid()) FOR [id]
GO
ALTER TABLE [dbo].[question_answer] ADD  DEFAULT (newid()) FOR [id]
GO
ALTER TABLE [dbo].[question_answer] ADD  DEFAULT (NULL) FOR [numeric_answer]
GO
ALTER TABLE [dbo].[research_area] ADD  DEFAULT (newid()) FOR [id]
GO
ALTER TABLE [dbo].[respondent_data] ADD  DEFAULT (newid()) FOR [id]
GO
ALTER TABLE [dbo].[respondent_data_option] ADD  DEFAULT (newid()) FOR [id]
GO
ALTER TABLE [dbo].[respondent_data_question] ADD  DEFAULT (newid()) FOR [id]
GO
ALTER TABLE [dbo].[respondent_to_group] ADD  DEFAULT (newid()) FOR [id]
GO
ALTER TABLE [dbo].[respondents_group] ADD  DEFAULT (newid()) FOR [id]
GO
ALTER TABLE [dbo].[section_to_user_group] ADD  DEFAULT (newid()) FOR [id]
GO
ALTER TABLE [dbo].[sensor_data] ADD  DEFAULT (newid()) FOR [id]
GO
ALTER TABLE [dbo].[survey] ADD  DEFAULT (newid()) FOR [id]
GO
ALTER TABLE [dbo].[survey] ADD  DEFAULT (sysutcdatetime()) FOR [creation_date]
GO
ALTER TABLE [dbo].[survey_participation] ADD  DEFAULT (newid()) FOR [id]
GO
ALTER TABLE [dbo].[survey_participation_time_slot] ADD  DEFAULT (newid()) FOR [id]
GO
ALTER TABLE [dbo].[survey_section] ADD  DEFAULT (newid()) FOR [id]
GO
ALTER TABLE [dbo].[survey_section] ADD  DEFAULT ((1)) FOR [display_on_one_screen]
GO
ALTER TABLE [dbo].[survey_sending_policy] ADD  DEFAULT (newid()) FOR [id]
GO
ALTER TABLE [dbo].[initial_survey_option]  WITH CHECK ADD FOREIGN KEY([question_id])
REFERENCES [dbo].[initial_survey_question] ([id])
ON DELETE CASCADE
GO
ALTER TABLE [dbo].[initial_survey_question]  WITH CHECK ADD FOREIGN KEY([survey_id])
REFERENCES [dbo].[initial_survey] ([id])
ON DELETE CASCADE
GO
ALTER TABLE [dbo].[localization_data]  WITH CHECK ADD FOREIGN KEY([participation_id])
REFERENCES [dbo].[survey_participation] ([id])
ON DELETE SET NULL
GO
ALTER TABLE [dbo].[localization_data]  WITH CHECK ADD FOREIGN KEY([respondent_id])
REFERENCES [dbo].[identity_user] ([id])
ON DELETE CASCADE
GO
ALTER TABLE [dbo].[number_range]  WITH CHECK ADD FOREIGN KEY([question_id])
REFERENCES [dbo].[question] ([id])
ON DELETE CASCADE
GO
ALTER TABLE [dbo].[option]  WITH CHECK ADD FOREIGN KEY([question_id])
REFERENCES [dbo].[question] ([id])
ON DELETE CASCADE
GO
ALTER TABLE [dbo].[option_selection]  WITH CHECK ADD FOREIGN KEY([option_id])
REFERENCES [dbo].[option] ([id])
GO
ALTER TABLE [dbo].[option_selection]  WITH CHECK ADD FOREIGN KEY([question_answer_id])
REFERENCES [dbo].[question_answer] ([id])
GO
ALTER TABLE [dbo].[question]  WITH CHECK ADD FOREIGN KEY([section_id])
REFERENCES [dbo].[survey_section] ([id])
ON DELETE CASCADE
GO
ALTER TABLE [dbo].[question_answer]  WITH CHECK ADD FOREIGN KEY([participation_id])
REFERENCES [dbo].[survey_participation] ([id])
ON DELETE CASCADE
GO
ALTER TABLE [dbo].[question_answer]  WITH CHECK ADD FOREIGN KEY([question_id])
REFERENCES [dbo].[question] ([id])
GO
ALTER TABLE [dbo].[respondent_data]  WITH CHECK ADD FOREIGN KEY([identity_user_id])
REFERENCES [dbo].[identity_user] ([id])
ON DELETE SET NULL
GO
ALTER TABLE [dbo].[respondent_data_option]  WITH CHECK ADD FOREIGN KEY([option_id])
REFERENCES [dbo].[initial_survey_option] ([id])
GO
ALTER TABLE [dbo].[respondent_data_option]  WITH CHECK ADD  CONSTRAINT [fk_respondent_data_option_respondent_data_question] FOREIGN KEY([respondent_data_question_id])
REFERENCES [dbo].[respondent_data_question] ([id])
ON DELETE CASCADE
GO
ALTER TABLE [dbo].[respondent_data_option] CHECK CONSTRAINT [fk_respondent_data_option_respondent_data_question]
GO
ALTER TABLE [dbo].[respondent_data_question]  WITH CHECK ADD FOREIGN KEY([question_id])
REFERENCES [dbo].[initial_survey_question] ([id])
GO
ALTER TABLE [dbo].[respondent_data_question]  WITH CHECK ADD FOREIGN KEY([respondent_id])
REFERENCES [dbo].[respondent_data] ([id])
ON DELETE CASCADE
GO
ALTER TABLE [dbo].[respondent_to_group]  WITH CHECK ADD FOREIGN KEY([group_id])
REFERENCES [dbo].[respondents_group] ([id])
ON DELETE CASCADE
GO
ALTER TABLE [dbo].[section_to_user_group]  WITH CHECK ADD FOREIGN KEY([group_id])
REFERENCES [dbo].[respondents_group] ([id])
ON DELETE CASCADE
GO
ALTER TABLE [dbo].[section_to_user_group]  WITH CHECK ADD FOREIGN KEY([section_id])
REFERENCES [dbo].[survey_section] ([id])
ON DELETE CASCADE
GO
ALTER TABLE [dbo].[sensor_data]  WITH CHECK ADD FOREIGN KEY([respondent_id])
REFERENCES [dbo].[identity_user] ([id])
ON DELETE CASCADE
GO
ALTER TABLE [dbo].[survey_participation]  WITH CHECK ADD FOREIGN KEY([respondent_id])
REFERENCES [dbo].[identity_user] ([id])
ON DELETE SET NULL
GO
ALTER TABLE [dbo].[survey_participation_time_slot]  WITH CHECK ADD FOREIGN KEY([survey_sending_policy_id])
REFERENCES [dbo].[survey_sending_policy] ([id])
ON DELETE CASCADE
GO
ALTER TABLE [dbo].[survey_section]  WITH CHECK ADD FOREIGN KEY([survey_id])
REFERENCES [dbo].[survey] ([id])
ON DELETE CASCADE
GO
ALTER TABLE [dbo].[survey_sending_policy]  WITH CHECK ADD FOREIGN KEY([survey_id])
REFERENCES [dbo].[survey] ([id])
ON DELETE CASCADE
GO